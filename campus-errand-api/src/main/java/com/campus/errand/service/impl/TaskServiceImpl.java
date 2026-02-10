package com.campus.errand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.errand.dto.TaskCancelDTO;
import com.campus.errand.dto.TaskPublishDTO;
import com.campus.errand.entity.Task;
import com.campus.errand.entity.Transaction;
import com.campus.errand.entity.User;
import com.campus.errand.entity.UserWallet;
import com.campus.errand.mapper.TaskMapper;
import com.campus.errand.mapper.UserMapper;
import com.campus.errand.service.MessageService;
import com.campus.errand.service.TaskService;
import com.campus.errand.service.TransactionService;
import com.campus.errand.service.UserWalletService;
import com.campus.errand.vo.TaskVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final UserWalletService userWalletService;
    private final TransactionService transactionService;
    private final MessageService messageService;

    @Autowired
    public TaskServiceImpl(UserMapper userMapper, StringRedisTemplate redisTemplate,
                          UserWalletService userWalletService, TransactionService transactionService,
                          MessageService messageService) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
        this.userWalletService = userWalletService;
        this.transactionService = transactionService;
        this.messageService = messageService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskVO publishTask(Long userId, TaskPublishDTO publishDTO) {
        // 1. 验证支付密码
        boolean passwordValid = userWalletService.verifyPayPassword(userId, publishDTO.getPayPassword());
        if (!passwordValid) {
            throw new RuntimeException("支付密码错误");
        }

        // 2. 计算总金额
        BigDecimal totalAmount = publishDTO.getReward();
        if (publishDTO.getWeightFee() != null) {
            totalAmount = totalAmount.add(publishDTO.getWeightFee());
        }
        if (publishDTO.getUrgencyFee() != null) {
            totalAmount = totalAmount.add(publishDTO.getUrgencyFee());
        }

        // 3. 检查余额是否充足
        boolean hasEnoughBalance = userWalletService.hasEnoughBalance(userId, totalAmount);
        if (!hasEnoughBalance) {
            throw new RuntimeException("余额不足，请先充值");
        }

        // 4. 冻结金额
        boolean freezeSuccess = userWalletService.freezeAmount(userId, totalAmount);
        if (!freezeSuccess) {
            throw new RuntimeException("冻结金额失败");
        }

        // 5. 获取用户钱包信息（用于记录余额）
        UserWallet wallet = userWalletService.getByUserId(userId);
        BigDecimal balanceAfter = wallet.getBalance().subtract(totalAmount);

        // 6. 创建任务
        Task task = new Task();
        BeanUtils.copyProperties(publishDTO, task);
        task.setUserId(userId);
        task.setTotalAmount(totalAmount);
        task.setStatus(0);
        save(task);

        // 7. 记录交易流水
        Transaction transaction = new Transaction();
        transaction.setTransactionNo(transactionService.generateTransactionNo());
        transaction.setUserId(userId);
        transaction.setDirection(2); // 支出
        transaction.setTransactionType(3); // 任务支付
        transaction.setAmount(totalAmount);
        transaction.setBalance(balanceAfter);
        transaction.setRelatedId(task.getId());
        transaction.setStatus(0); // 待结算
        transaction.setRemark("发布任务冻结：" + task.getTitle());
        transaction.setCreateTime(LocalDateTime.now());
        transactionService.save(transaction);

        return convertToVO(task);
    }

    @Override
    public IPage<TaskVO> getTaskList(Integer taskType, Integer status, Long current, Long size) {
        Page<Task> page = new Page<>(current, size);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();

        // 只查询待接单的任务
        wrapper.eq(Task::getStatus, 0);

        if (taskType != null) {
            wrapper.eq(Task::getTaskType, taskType);
        }

        wrapper.orderByDesc(Task::getCreateTime);

        IPage<Task> taskPage = page(page, wrapper);

        // 转换为VO
        List<TaskVO> voList = taskPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        Page<TaskVO> resultPage = new Page<>();
        resultPage.setCurrent(taskPage.getCurrent());
        resultPage.setSize(taskPage.getSize());
        resultPage.setTotal(taskPage.getTotal());
        resultPage.setRecords(voList);

        return resultPage;
    }

    @Override
    public TaskVO getTaskDetail(Long taskId) {
        Task task = getById(taskId);
        if (task == null) {
            return null;
        }
        return convertToVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptTask(Long taskId, Long runnerId) {
        // 使用Redis分布式锁防止超卖
        String lockKey = "task:lock:" + taskId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);

        if (!Boolean.TRUE.equals(locked)) {
            return false;
        }

        try {
            Task task = getById(taskId);
            if (task == null) {
                return false;
            }

            // 验证任务状态
            if (task.getStatus() != 0) {
                return false;
            }

            // 不能接自己的单
            if (task.getUserId().equals(runnerId)) {
                return false;
            }

            // 更新任务状态
            task.setRunnerId(runnerId);
            task.setStatus(1); // 已接单
            task.setAcceptTime(LocalDateTime.now());

            boolean success = updateById(task);

            // 发送消息通知发单者
            if (success) {
                User runner = userMapper.selectById(runnerId);
                String runnerName = runner != null ? runner.getNickname() : "未知用户";
                messageService.sendTaskAcceptedNotification(task.getUserId(), taskId, task.getTitle(), runnerName);
            }

            return success;
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTaskStatus(Long taskId, Integer status, Long userId) {
        Task task = getById(taskId);
        if (task == null) {
            return false;
        }

        // 验证权限（发单者或接单者）
        if (!task.getUserId().equals(userId) && !userId.equals(task.getRunnerId())) {
            return false;
        }

        // 验证状态流转是否合法
        if (!isValidStatusTransition(task.getStatus(), status)) {
            return false;
        }

        task.setStatus(status);

        // 更新相应的时间字段
        switch (status) {
            case 2: // 待取件
                task.setPickupTime(LocalDateTime.now());
                break;
            case 3: // 配送中
                break;
            case 4: // 待确认
                task.setDeliveryTime(LocalDateTime.now());
                break;
            case 5: // 已完成
                task.setCompleteTime(LocalDateTime.now());
                // 任务完成，进行结算
                settleTask(task);
                break;
        }

        return updateById(task);
    }

    /**
     * 任务结算
     */
    @Transactional(rollbackFor = Exception.class)
    public void settleTask(Task task) {
        Long publisherId = task.getUserId();
        Long runnerId = task.getRunnerId();
        BigDecimal totalAmount = task.getTotalAmount();

        // 1. 解冻发单者金额
        userWalletService.unfreezeAmount(publisherId, totalAmount);

        // 2. 扣除发单者余额
        userWalletService.deductBalance(publisherId, totalAmount);

        // 3. 增加跑腿员余额（全额到账，平台暂不抽成）
        userWalletService.addBalance(runnerId, totalAmount);

        // 4. 更新发单者交易流水为成功
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getRelatedId, task.getId())
               .eq(Transaction::getUserId, publisherId)
               .eq(Transaction::getTransactionType, 3);
        Transaction publisherTx = transactionService.getOne(wrapper);
        if (publisherTx != null) {
            publisherTx.setStatus(1); // 成功
            transactionService.updateById(publisherTx);
        }

        // 5. 创建跑腿员收入流水
        Transaction runnerTx = new Transaction();
        runnerTx.setTransactionNo(transactionService.generateTransactionNo());
        runnerTx.setUserId(runnerId);
        runnerTx.setDirection(1); // 收入
        runnerTx.setTransactionType(4); // 任务收入
        runnerTx.setAmount(totalAmount);
        UserWallet runnerWallet = userWalletService.getByUserId(runnerId);
        runnerTx.setBalance(runnerWallet.getBalance());
        runnerTx.setRelatedId(task.getId());
        runnerTx.setStatus(1); // 成功
        runnerTx.setRemark("完成任务收入：" + task.getTitle());
        runnerTx.setCreateTime(LocalDateTime.now());
        transactionService.save(runnerTx);
    }

    private boolean isValidStatusTransition(Integer currentStatus, Integer newStatus) {
        // 状态流转规则
        switch (currentStatus) {
            case 0: // 待接单
                return newStatus == 1 || newStatus == 6;
            case 1: // 已接单
                return newStatus == 2 || newStatus == 6;
            case 2: // 待取件
                return newStatus == 3 || newStatus == 6;
            case 3: // 配送中
                return newStatus == 4;
            case 4: // 待确认
                return newStatus == 5;
            default:
                return false;
        }
    }

    private TaskVO convertToVO(Task task) {
        TaskVO vo = new TaskVO();
        BeanUtils.copyProperties(task, vo);

        // 设置任务类型名称
        vo.setTaskTypeName(getTaskTypeName(task.getTaskType()));

        // 设置状态名称
        vo.setStatusName(getStatusName(task.getStatus()));

        // 查询发布者信息
        User publisher = userMapper.selectById(task.getUserId());
        if (publisher != null) {
            vo.setPublisherName(publisher.getNickname());
            vo.setPublisherAvatar(publisher.getAvatar());
            vo.setPublisherCreditScore(publisher.getCreditScore());
        }

        // 查询接单者信息
        if (task.getRunnerId() != null) {
            User runner = userMapper.selectById(task.getRunnerId());
            if (runner != null) {
                vo.setRunnerName(runner.getNickname());
                vo.setRunnerAvatar(runner.getAvatar());
            }
        }

        return vo;
    }

    private String getTaskTypeName(Integer taskType) {
        switch (taskType) {
            case 1: return "取快递";
            case 2: return "代买";
            case 3: return "送件";
            case 4: return "其他";
            default: return "未知";
        }
    }

    private String getStatusName(Integer status) {
        switch (status) {
            case 0: return "待接单";
            case 1: return "已接单";
            case 2: return "待取件";
            case 3: return "配送中";
            case 4: return "待确认";
            case 5: return "已完成";
            case 6: return "已取消";
            default: return "未知";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelTask(Long taskId, Long userId, TaskCancelDTO cancelDTO) {
        Task task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        Integer cancelType = cancelDTO.getCancelType();
        String reason = cancelDTO.getReason();

        if (cancelType == 1) {
            // 发单者取消
            return cancelByPublisher(task, userId, reason);
        } else if (cancelType == 2) {
            // 跑腿员取消
            return cancelByRunner(task, userId, reason);
        } else {
            throw new RuntimeException("无效的取消类型");
        }
    }

    /**
     * 发单者取消任务
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelByPublisher(Task task, Long userId, String reason) {
        // 1. 验证权限
        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("无权取消该任务");
        }

        // 2. 验证状态（待接单、已接单、待取件可以取消）
        Integer status = task.getStatus();
        if (status != 0 && status != 1 && status != 2) {
            throw new RuntimeException("当前状态不能取消");
        }

        Long publisherId = task.getUserId();
        BigDecimal totalAmount = task.getTotalAmount();

        // 3. 解冻金额
        boolean unfreezeSuccess = userWalletService.unfreezeAmount(publisherId, totalAmount);
        if (!unfreezeSuccess) {
            throw new RuntimeException("解冻金额失败");
        }

        // 4. 查找并更新原交易流水
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getRelatedId, task.getId())
               .eq(Transaction::getUserId, publisherId)
               .eq(Transaction::getTransactionType, 3);
        Transaction publisherTx = transactionService.getOne(wrapper);
        if (publisherTx != null) {
            publisherTx.setStatus(2); // 失败（已取消）
            publisherTx.setRemark(publisherTx.getRemark() + " - 任务取消退款");
            transactionService.updateById(publisherTx);
        }

        // 5. 创建退款流水
        UserWallet wallet = userWalletService.getByUserId(publisherId);
        Transaction refundTx = new Transaction();
        refundTx.setTransactionNo(transactionService.generateTransactionNo());
        refundTx.setUserId(publisherId);
        refundTx.setDirection(1); // 收入
        refundTx.setTransactionType(5); // 退款
        refundTx.setAmount(totalAmount);
        refundTx.setBalance(wallet.getBalance());
        refundTx.setRelatedId(task.getId());
        refundTx.setStatus(1); // 成功
        refundTx.setRemark("任务取消退款：" + task.getTitle() + (reason != null ? " - " + reason : ""));
        refundTx.setCreateTime(LocalDateTime.now());
        transactionService.save(refundTx);

        // 6. 更新任务状态
        task.setStatus(6); // 已取消
        task.setCancelTime(LocalDateTime.now());
        task.setCancelType(1); // 发单者取消
        task.setCancelReason(reason);

        return updateById(task);
    }

    /**
     * 跑腿员取消任务
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelByRunner(Task task, Long userId, String reason) {
        // 1. 验证权限
        if (task.getRunnerId() == null || !task.getRunnerId().equals(userId)) {
            throw new RuntimeException("无权取消该任务");
        }

        // 2. 验证状态（已接单、待取件可以取消）
        Integer status = task.getStatus();
        if (status != 1 && status != 2) {
            throw new RuntimeException("当前状态不能取消");
        }

        // 3. 清除接单者信息
        task.setRunnerId(null);
        task.setAcceptTime(null);

        // 4. 任务状态回退为待接单
        task.setStatus(0);
        task.setCancelTime(LocalDateTime.now());
        task.setCancelType(2); // 跑腿员取消
        task.setCancelReason(reason);

        // 5. 记录取消次数（可选，用于信用评估）
        // TODO: 记录跑腿员取消次数

        return updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deliverTask(Long taskId, Long runnerId) {
        Task task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 1. 验证权限（必须是接单者）
        if (task.getRunnerId() == null || !task.getRunnerId().equals(runnerId)) {
            throw new RuntimeException("无权操作该任务");
        }

        // 2. 验证状态（必须是配送中）
        if (task.getStatus() != 3) {
            throw new RuntimeException("当前状态不能确认送达");
        }

        // 3. 更新任务状态为待确认
        task.setStatus(4); // 待确认
        task.setDeliveryTime(LocalDateTime.now());

        boolean success = updateById(task);

        // 发送消息通知发单者
        if (success) {
            messageService.sendDeliveredNotification(task.getUserId(), taskId, task.getTitle());
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveTask(Long taskId, Long userId) {
        Task task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 1. 验证权限（必须是发单者）
        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该任务");
        }

        // 2. 验证状态（必须是待确认）
        if (task.getStatus() != 4) {
            throw new RuntimeException("当前状态不能确认收货");
        }

        // 3. 任务结算
        settleTask(task);

        // 4. 更新任务状态为已完成
        task.setStatus(5); // 已完成
        task.setCompleteTime(LocalDateTime.now());

        boolean success = updateById(task);

        // 发送消息通知跑腿员
        if (success) {
            messageService.sendReceivedNotification(task.getRunnerId(), taskId, task.getTitle());
        }

        return success;
    }

    @Override
    public IPage<TaskVO> getMyTasks(Long userId, Integer role, Integer status, Long current, Long size) {
        Page<Task> page = new Page<>(current, size);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();

        // 根据角色筛选
        if (role == 1) {
            // 发单者
            wrapper.eq(Task::getUserId, userId);
        } else if (role == 2) {
            // 接单者
            wrapper.eq(Task::getRunnerId, userId);
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }

        wrapper.orderByDesc(Task::getCreateTime);

        IPage<Task> taskPage = page(page, wrapper);

        // 转换为VO
        List<TaskVO> voList = taskPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        Page<TaskVO> resultPage = new Page<>();
        resultPage.setCurrent(taskPage.getCurrent());
        resultPage.setSize(taskPage.getSize());
        resultPage.setTotal(taskPage.getTotal());
        resultPage.setRecords(voList);

        return resultPage;
    }
}
