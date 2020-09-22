// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.lambda.deploy

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ex.ProgressIndicatorEx
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import software.aws.toolkits.jetbrains.ui.ProgressPanel
import java.awt.AWTEvent
import java.awt.BorderLayout
import java.awt.event.AWTEventListener
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class SamDeployView(private val project: Project, private val progressIndicator: ProgressIndicatorEx) : Disposable {
    lateinit var content: JPanel
    lateinit var progressPanel: ProgressPanel
    lateinit var logTabs: JBTabbedPane
    private val consoleViews = mutableListOf<ConsoleView>()
    private var manuallySelectedTab = false

    private fun createUIComponents() {
        progressPanel = ProgressPanel(progressIndicator)
        logTabs = JBTabbedPane()
        logTabs.tabComponentInsets = JBUI.emptyInsets()
    }

    fun addLogTab(title: String?): ConsoleView {
        val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).let {
            it.setViewer(false)
            it.console
        }
        consoleViews.add(console)
        runInEdt {
            val consoleComponent = console.component.apply {
                border = IdeBorderFactory.createBorder()
            }
            val toolbarActions = DefaultActionGroup().apply {
                addAll(*console.createConsoleActions())
            }
            val logPanel = JPanel(BorderLayout()).apply {
                border = null
                val toolbar = ActionManager.getInstance().createActionToolbar("SamDeployLogs", toolbarActions, false)
                add(toolbar.component, BorderLayout.WEST)
                add(consoleComponent, BorderLayout.CENTER)
            }

            // Serves the purpose of looking for click events that are on a child of the tab, so that we
            // disable auto-switching tabs as progress proceeds
            UIUtil.addAwtListener(AWTEventListener { event: AWTEvent ->
                val mouseEvent = event as MouseEvent
                if (!UIUtil.isActionClick(mouseEvent)) {
                    return@AWTEventListener
                }
                if (SwingUtilities.isDescendingFrom(mouseEvent.component, logPanel)) {
                    manuallySelectedTab = true
                }
            }, AWTEvent.MOUSE_EVENT_MASK, console)

            logTabs.addTab(title, logPanel)

            if (!manuallySelectedTab) {
                logTabs.selectedIndex = logTabs.tabCount - 1
            }
        }
        return console
    }

    override fun dispose() {
        consoleViews.forEach { it.dispose() }
    }
}
