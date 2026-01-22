package com.wemod.automation.ui.components.sidebar

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight

import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

/**
 * 动画侧边栏头部
 */
@Composable
fun SidebarHeader(
    isVisible: Boolean,
    title: String = "工具与设置",
    subtitle: String = "不常用功能和系统设置"
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        ) + slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + slideOutHorizontally(
            targetOffsetX = { -it / 3 },
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "设置",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 动画分组标题
 */
@Composable
fun AnimatedSectionTitle(
    isVisible: Boolean,
    title: String,
    delay: Int = 0
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = delay,
                easing = FastOutSlowInEasing
            )
        ) + slideInHorizontally(
            initialOffsetX = { -it / 4 },
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = delay,
                easing = FastOutSlowInEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + slideOutHorizontally(
            targetOffsetX = { -it / 4 },
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

/**
 * 动画开关行
 */
@Composable
fun AnimatedSwitchRow(
    isVisible: Boolean,
    item: SwitchItem
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = item.animationDelay,
                easing = FastOutSlowInEasing
            )
        ) + slideInHorizontally(
            initialOffsetX = { it / 3 },
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = item.animationDelay,
                easing = FastOutSlowInEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + slideOutHorizontally(
            targetOffsetX = { it / 3 },
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        SwitchRow(item = item)
    }
}

/**
 * 动画按钮行
 */
@Composable
fun AnimatedButtonRow(
    isVisible: Boolean,
    item: ButtonItem
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = item.animationDelay,
                easing = FastOutSlowInEasing
            )
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = item.animationDelay,
                easing = FastOutSlowInEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + slideOutVertically(
            targetOffsetY = { it / 4 },
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        ButtonRow(item = item)
    }
}

/**
 * 开关行组件
 */
@Composable
fun SwitchRow(item: SwitchItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            item.icon,
            contentDescription = item.title,
            modifier = Modifier.size(24.dp),
            tint = if (item.enabled) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (item.enabled) MaterialTheme.colorScheme.onSurface 
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            
            Text(
                text = item.description,
                fontSize = 12.sp,
                color = if (item.enabled) MaterialTheme.colorScheme.onSurfaceVariant 
                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        }
        
        val onCheckedChange: ((Boolean) -> Unit)? = if (item.enabled) {
            { _ -> item.onToggle() }
        } else {
            null
        }
        
        Switch(
            checked = item.checked,
            onCheckedChange = onCheckedChange,
            enabled = item.enabled
        )
    }
}

/**
 * 按钮行组件
 */
@Composable
fun ButtonRow(item: ButtonItem) {
    val clickHandler: () -> Unit = if (item.enabled) {
        { item.onClick() }
    } else {
        {}
    }
    
    Surface(
        onClick = clickHandler,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        enabled = item.enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(24.dp),
                tint = if (item.enabled) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (item.enabled) MaterialTheme.colorScheme.onSurface 
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
                
                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = if (item.enabled) MaterialTheme.colorScheme.onSurfaceVariant 
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
            
            if (item.enabled) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "进入",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
