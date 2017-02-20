import React, { Component } from 'react'
import { requireNativeComponent } from 'react-native'
import resolveAssetSource from 'resolveAssetSource'

const RNBottomNavigation = requireNativeComponent('RNBottomNavigation', null, {
  nativeOnly: {
    onChange: true,
    accessibilityLabel: true,
    testID: true,
    importantForAccessibility: true,
    renderToHardwareTextureAndroid: true,
    onLayout: true,
    accessibilityLiveRegion: true,
    accessibilityComponentType: true,
  }
})

class BottomNavigation extends Component {
  _onChange = (event) => {
    if (this.props.onTabSelected) {
      this.props.onTabSelected(event.nativeEvent.selectedPosition)
    }
  }

  render() {
    const nativeProps = { ...this.props }
    if (this.props.tabs) {
      const tabs = []
      for (let i = 0; i < this.props.tabs.length; i++) {
        const tab = {
          ...this.props.tabs[i]
        }
        if (tab.icon) {
          tab.icon = resolveAssetSource(tab.icon)
        }
        if (tab.disabled == null) {
          tab.disabled = false
        }
        tabs.push(tab);
      }
      nativeProps.tabs = tabs
    }

    return <RNBottomNavigation {...nativeProps} onChange={this._onChange} />
  }
}

const colorList = React.PropTypes.shape({
  default: React.PropTypes.string,
  active: React.PropTypes.string
})

BottomNavigation.propTypes = {
  tabs: React.PropTypes.arrayOf(React.PropTypes.shape({
    title: React.PropTypes.string.isRequired,
    icon: React.PropTypes.any.isRequired,
    disabled: React.PropTypes.bool
  })).isRequired,
  labelColors: colorList,
  iconTint: colorList,
  activeTab: React.PropTypes.number,
  onTabSelected: React.PropTypes.func
}

export default BottomNavigation
