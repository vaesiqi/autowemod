// 新文件：app/src/main/kotlin/com/wemod/automation/ui/components/sidebar/SidebarDrawer.kt
package com.wemod.automation.ui.components.sidebar

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 组件化侧边栏抽屉
 * 支持动态配置菜单项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarDrawer(
    drawerState: DrawerState,
    sections: List<Section>,
    content: @Composable () -> Unit
) {
    // 跟踪抽屉是否打开
    val isDrawerOpen by remember(drawerState.currentValue) {
        derivedStateOf { drawerState.isOpen }
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidebarContent(
                isVisible = isDrawerOpen,
                sections = sections
            )
        },
        gesturesEnabled = true
    ) {
        content()
    }
}

/**
 * 侧边栏内容
 */
@Composable
fun SidebarContent(
    isVisible: Boolean,
    sections: List<Section>
) {
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. 头部
            SidebarHeader(isVisible = isVisible)
            
            // 2. 动态渲染所有分组
            sections.forEachIndexed { index, section ->
                if (index > 0) {
                    Divider()
                }
                
                // 分组内容
                SectionContent(
                    isVisible = isVisible,
                    section = section
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 分组内容
 */
@Composable
fun SectionContent(
    isVisible: Boolean,
    section: Section
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // 分组标题
        AnimatedSectionTitle(
            isVisible = isVisible,
            title = section.title,
            delay = section.animationDelay
        )
        
        // 渲染分组内的所有菜单项
        section.items.forEach { item ->
            when (item) {
                is SwitchItem -> {
                    AnimatedSwitchRow(
                        isVisible = isVisible,
                        item = item
                    )
                }
                is ButtonItem -> {
                    AnimatedButtonRow(
                        isVisible = isVisible,
                        item = item
                    )
                }
            }
        }
    }
}