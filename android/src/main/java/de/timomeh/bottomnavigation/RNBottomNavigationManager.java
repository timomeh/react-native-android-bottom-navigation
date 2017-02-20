package de.timomeh.bottomnavigation;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by @timomeh on 19/02/2017.
 */

public class RNBottomNavigationManager extends ViewGroupManager<RNBottomNavigation> {

    private static final String REACT_CLASS = "RNBottomNavigation";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected RNBottomNavigation createViewInstance(final ThemedReactContext reactContext) {
        final RNBottomNavigation bottomNavigation = new RNBottomNavigation(reactContext);

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                WritableMap event = Arguments.createMap();
                event.putInt("selectedPosition", item.getOrder());
                reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                        bottomNavigation.getId(),
                        "topChange",
                        event
                );

                return true;
            }
        });

        return bottomNavigation;
    }

    @ReactProp(name = "tabs")
    public void setTabs(RNBottomNavigation view, @Nullable ReadableArray actions) {
        view.setActions(actions);
    }

    @ReactProp(name = "labelColors")
    public void setItemTextColor(RNBottomNavigation view, ReadableMap colorMap) {
        view.setItemTextColor(buildColorStateList(colorMap));
    }

    @ReactProp(name = "iconTint")
    public void setItemIconTintColor(RNBottomNavigation view, ReadableMap colorMap) {
        view.setItemIconTintList(buildColorStateList(colorMap));
    }

    @ReactProp(name = "activeTab", defaultInt = 0)
    public void setActiveTab(RNBottomNavigation view, int activeTab) {
        Menu menu = view.getMenu();

        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            if (i == activeTab) {
                menuItem.setChecked(true);
            } else {
                menuItem.setChecked(false);
            }
        }
    }

    private HashMap<String, String> unifyColors(ReadableMap colorMap) {
        String normal = colorMap.getString("default");
        String active = colorMap.hasKey("active") ? colorMap.getString("active") : normal;
        String disabled = colorMap.hasKey("disabled") ? colorMap.getString("disabled") : normal;

        HashMap<String, String> map = new HashMap<>();
        map.put("default", normal);
        map.put("active", active);
        map.put("disabled", disabled);

        return map;
    }

    private ColorStateList buildColorStateList(ReadableMap colorMap) {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] {-android.R.attr.state_enabled },
                new int[] { },
        };

        Map<String, String> colorStates = unifyColors(colorMap);

        int[] colors = new int[] {
                Color.parseColor(colorStates.get("active")),
                Color.parseColor(colorStates.get("disabled")),
                Color.parseColor(colorStates.get("default"))
        };

        return new ColorStateList(states, colors);
    }

}
