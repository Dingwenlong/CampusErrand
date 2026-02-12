<template>
  <view class="container">
    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="keyword"
          placeholder="搜索地点"
          confirm-type="search"
          @confirm="searchLocation"
        />
        <text v-if="keyword" class="clear-icon" @click="clearSearch">×</text>
      </view>
      <text class="cancel-btn" @click="goBack">取消</text>
    </view>

    <!-- 地图区域 -->
    <view class="map-section">
      <map
        id="locationMap"
        class="map"
        :longitude="currentLocation.longitude"
        :latitude="currentLocation.latitude"
        :markers="markers"
        :show-location="true"
        @markertap="onMarkerTap"
        @regionchange="onRegionChange"
        @tap="onMapTap"
      />
      <!-- 中心定位标记 -->
      <view class="center-marker">
        <view class="marker-pin"></view>
      </view>
    </view>

    <!-- 地址信息卡片 -->
    <view class="address-card" v-if="selectedAddress.name">
      <view class="address-info">
        <text class="address-name">{{ selectedAddress.name }}</text>
        <text class="address-detail">{{ selectedAddress.address }}</text>
      </view>
      <button class="confirm-btn" @click="confirmLocation">确认选择</button>
    </view>

    <!-- 附近地点列表 -->
    <view class="nearby-section">
      <view class="section-header">
        <text class="section-title">附近地点</text>
        <view class="location-btn" @click="getCurrentLocation">
          <text class="location-icon">📍</text>
          <text class="location-text">定位</text>
        </view>
      </view>
      <scroll-view class="nearby-list" scroll-y>
        <view
          v-for="(item, index) in nearbyList"
          :key="index"
          class="nearby-item"
          :class="{ active: selectedIndex === index }"
          @click="selectLocation(item, index)"
        >
          <view class="item-icon">📍</view>
          <view class="item-info">
            <text class="item-name">{{ item.name }}</text>
            <text class="item-address">{{ item.address }}</text>
          </view>
          <text v-if="selectedIndex === index" class="check-icon">✓</text>
        </view>
        <view v-if="nearbyList.length === 0" class="empty-tip">
          <text>暂无附近地点</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
// 腾讯地图 SDK key（可通过 manifest / 环境变量注入）
const MAP_KEY = (typeof process !== 'undefined' && process.env && process.env.UNI_APP_TENCENT_MAP_KEY) || ''

export default {
  data() {
    return {
      keyword: '',
      currentLocation: {
        latitude: 39.90469, // 默认北京
        longitude: 116.40717
      },
      selectedLocation: {
        latitude: 39.90469,
        longitude: 116.40717
      },
      selectedAddress: {
        name: '',
        address: ''
      },
      selectedIndex: -1,
      nearbyList: [],
      markers: [],
      mapContext: null,
      fromPage: '', // 来源页面标记
      addressType: '', // 地址类型：pickup-取件地址, delivery-送达地址
      hasMapKey: !!MAP_KEY // 是否配置了腾讯地图 key
    }
  },
  onLoad(options) {
    // 获取来源页面信息
    if (options.from) {
      this.fromPage = options.from
    }
    if (options.type) {
      this.addressType = options.type
    }
    
    // 初始化地图上下文
    this.mapContext = uni.createMapContext('locationMap', this)

    if (!this.hasMapKey) {
      uni.showToast({
        title: '未配置地图KEY，启用基础定位模式',
        icon: 'none'
      })
    }
    
    // 获取当前位置
    this.getCurrentLocation()
  },
  methods: {
    // 获取当前定位
    getCurrentLocation() {
      uni.showLoading({ title: '定位中...' })
      
      uni.getLocation({
        type: 'gcj02',
        success: (res) => {
          this.currentLocation = {
            latitude: res.latitude,
            longitude: res.longitude
          }
          this.selectedLocation = {
            latitude: res.latitude,
            longitude: res.longitude
          }
          
          // 逆地址解析获取地址信息
          this.reverseGeocoder(res.latitude, res.longitude)
          
          // 搜索附近地点
          this.searchNearby(res.latitude, res.longitude)
        },
        fail: (err) => {
          console.error('定位失败:', err)
          uni.showToast({
            title: '定位失败，请检查定位权限',
            icon: 'none'
          })
        },
        complete: () => {
          uni.hideLoading()
        }
      })
    },

    // 逆地址解析
    reverseGeocoder(latitude, longitude) {
      if (!this.hasMapKey) {
        this.selectedAddress = {
          name: '当前位置',
          address: `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`
        }
        return
      }

      // 使用腾讯地图 WebService API
      const url = `https://apis.map.qq.com/ws/geocoder/v1/?location=${latitude},${longitude}&key=${MAP_KEY}&get_poi=1`
      
      uni.request({
        url,
        success: (res) => {
          if (res.data.status === 0) {
            const result = res.data.result
            this.selectedAddress = {
              name: result.formatted_addresses?.recommend || result.address,
              address: result.address
            }
          }
        },
        fail: (err) => {
          console.error('逆地址解析失败:', err)
        }
      })
    },

    // 搜索附近地点
    searchNearby(latitude, longitude) {
      if (!this.hasMapKey) {
        this.nearbyList = [{
          name: '当前位置',
          address: `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`,
          location: { lat: latitude, lng: longitude }
        }]
        this.updateMarkers()
        return
      }

      const url = `https://apis.map.qq.com/ws/place/v1/search?keyword=小区&boundary=nearby(${latitude},${longitude},1000)&page_size=20&key=${MAP_KEY}`
      
      uni.request({
        url,
        success: (res) => {
          if (res.data.status === 0) {
            this.nearbyList = res.data.data || []
            // 更新标记点
            this.updateMarkers()
          }
        },
        fail: (err) => {
          console.error('搜索附近地点失败:', err)
        }
      })
    },

    // 搜索地点
    searchLocation() {
      if (!this.hasMapKey) {
        uni.chooseLocation({
          success: (res) => {
            const item = {
              name: res.name || '选择位置',
              address: res.address || `${res.latitude.toFixed(6)}, ${res.longitude.toFixed(6)}`,
              location: { lat: res.latitude, lng: res.longitude }
            }
            this.nearbyList = [item]
            this.selectLocation(item, 0)
          },
          fail: () => {
            uni.showToast({
              title: '请选择定位',
              icon: 'none'
            })
          }
        })
        return
      }

      if (!this.keyword.trim()) {
        uni.showToast({
          title: '请输入搜索关键词',
          icon: 'none'
        })
        return
      }

      uni.showLoading({ title: '搜索中...' })

      const { latitude, longitude } = this.currentLocation
      const url = `https://apis.map.qq.com/ws/place/v1/search?keyword=${encodeURIComponent(this.keyword)}&boundary=nearby(${latitude},${longitude},5000)&page_size=20&key=${MAP_KEY}`

      uni.request({
        url,
        success: (res) => {
          if (res.data.status === 0) {
        this.nearbyList = res.data.data || []
        this.updateMarkers()
            
            if (this.nearbyList.length > 0) {
              // 移动地图到第一个结果
              const first = this.nearbyList[0]
              this.moveToLocation(first.location.lat, first.location.lng)
            }
          } else {
            uni.showToast({
              title: '搜索失败',
              icon: 'none'
            })
          }
        },
        fail: (err) => {
          console.error('搜索失败:', err)
          uni.showToast({
            title: '搜索失败',
            icon: 'none'
          })
        },
        complete: () => {
          uni.hideLoading()
        }
      })
    },

    // 清空搜索
    clearSearch() {
      this.keyword = ''
      // 重新加载附近地点
      const { latitude, longitude } = this.currentLocation
      this.searchNearby(latitude, longitude)
    },

    // 更新地图标记
    updateMarkers() {
      this.markers = this.nearbyList.map((item, index) => ({
        id: index,
        latitude: item.location.lat,
        longitude: item.location.lng,
        title: item.name,
        iconPath: '/static/images/marker.png',
        width: 30,
        height: 30
      }))
    },

    // 选择地点
    selectLocation(item, index) {
      this.selectedIndex = index
      this.selectedLocation = {
        latitude: item.location.lat,
        longitude: item.location.lng
      }
      this.selectedAddress = {
        name: item.name,
        address: item.address
      }
      
      // 移动地图到选中位置
      this.moveToLocation(item.location.lat, item.location.lng)
    },

    // 移动地图到指定位置
    moveToLocation(latitude, longitude) {
      this.mapContext.moveToLocation({
        latitude,
        longitude,
        success: () => {
          console.log('地图移动成功')
        }
      })
    },

    // 地图视野变化
    onRegionChange(e) {
      if (e.type === 'end') {
        // 获取地图中心点
        this.mapContext.getCenterLocation({
          success: (res) => {
            this.selectedLocation = {
              latitude: res.latitude,
              longitude: res.longitude
            }
            // 逆地址解析
            this.reverseGeocoder(res.latitude, res.longitude)
          }
        })
      }
    },

    // 点击地图
    onMapTap(e) {
      const { latitude, longitude } = e.detail
      this.selectedLocation = { latitude, longitude }
      this.reverseGeocoder(latitude, longitude)
      this.selectedIndex = -1
    },

    // 点击标记
    onMarkerTap(e) {
      const markerId = e.detail.markerId
      const item = this.nearbyList[markerId]
      if (item) {
        this.selectLocation(item, markerId)
      }
    },

    // 确认选择位置
    confirmLocation() {
      if (!this.selectedAddress.name) {
        uni.showToast({
          title: '请选择地址',
          icon: 'none'
        })
        return
      }

      const locationData = {
        name: this.selectedAddress.name,
        address: this.selectedAddress.address,
        latitude: this.selectedLocation.latitude,
        longitude: this.selectedLocation.longitude,
        type: this.addressType
      }

      // 将选择的位置信息存储到全局或返回上一页
      const pages = getCurrentPages()
      const prevPage = pages[pages.length - 2]
      
      if (prevPage) {
        // 设置上一页的数据
        prevPage.$vm.selectedLocation = locationData
        
        // 触发上一页的回调
        if (prevPage.$vm.onLocationSelected) {
          prevPage.$vm.onLocationSelected(locationData)
        }
      }

      // 返回上一页
      uni.navigateBack()
    },

    // 返回上一页
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: var(--color-bg);
}

/* 搜索栏 */
.search-section {
  display: flex;
  align-items: center;
  padding: var(--space-4) var(--space-6);
  background-color: var(--color-surface);
  border-bottom: 2rpx solid var(--color-divider);
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: var(--color-bg-secondary);
  border-radius: var(--radius-full);
  padding: var(--space-3) var(--space-4);
}

.search-icon {
  font-size: var(--font-size-base);
  margin-right: var(--space-3);
  color: var(--color-text-tertiary);
}

.search-input {
  flex: 1;
  font-size: var(--font-size-base);
  color: var(--color-text-primary);
}

.clear-icon {
  font-size: var(--font-size-lg);
  color: var(--color-text-tertiary);
  padding: var(--space-2);
}

.cancel-btn {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  margin-left: var(--space-4);
}

/* 地图区域 */
.map-section {
  position: relative;
  height: 400rpx;
}

.map {
  width: 100%;
  height: 100%;
}

.center-marker {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -100%);
  pointer-events: none;
}

.marker-pin {
  width: 40rpx;
  height: 60rpx;
  background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  border-radius: 20rpx 20rpx 20rpx 0;
  transform: rotate(-45deg);
  box-shadow: 0 4rpx 12rpx rgba(255, 195, 0, 0.4);

  &::after {
    content: '';
    position: absolute;
    width: 16rpx;
    height: 16rpx;
    background-color: #fff;
    border-radius: 50%;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%) rotate(45deg);
  }
}

/* 地址卡片 */
.address-card {
  background-color: var(--color-surface);
  padding: var(--space-5) var(--space-6);
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 2rpx solid var(--color-divider);
}

.address-info {
  flex: 1;
  margin-right: var(--space-4);
}

.address-name {
  display: block;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-2);
}

.address-detail {
  display: block;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.confirm-btn {
  width: 160rpx;
  height: 72rpx;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  border: none;
}

/* 附近地点列表 */
.nearby-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: var(--color-surface);
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-6);
  border-bottom: 2rpx solid var(--color-divider);
}

.section-title {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.location-btn {
  display: flex;
  align-items: center;
  padding: var(--space-2) var(--space-4);
  background-color: var(--color-bg-secondary);
  border-radius: var(--radius-md);
}

.location-icon {
  font-size: var(--font-size-sm);
  margin-right: var(--space-1);
}

.location-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.nearby-list {
  flex: 1;
  padding: 0 var(--space-6);
}

.nearby-item {
  display: flex;
  align-items: center;
  padding: var(--space-5) 0;
  border-bottom: 2rpx solid var(--color-divider);

  &:last-child {
    border-bottom: none;
  }

  &.active {
    .item-name {
      color: var(--color-primary-dark);
    }
  }
}

.item-icon {
  font-size: var(--font-size-lg);
  margin-right: var(--space-4);
  color: var(--color-text-tertiary);
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-name {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  margin-bottom: var(--space-1);
}

.item-address {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.check-icon {
  font-size: var(--font-size-lg);
  color: var(--color-primary);
  font-weight: var(--font-weight-bold);
}

.empty-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) 0;
  
  text {
    font-size: var(--font-size-base);
    color: var(--color-text-tertiary);
  }
}
</style>
