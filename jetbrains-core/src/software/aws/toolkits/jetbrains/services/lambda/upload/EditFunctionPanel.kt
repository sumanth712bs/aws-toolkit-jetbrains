// Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0
package software.aws.toolkits.jetbrains.services.lambda.upload

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.SortedComboBoxModel
import software.amazon.awssdk.services.lambda.model.Runtime
import software.amazon.awssdk.services.s3.model.Bucket
import software.aws.toolkits.jetbrains.services.iam.IamResources
import software.aws.toolkits.jetbrains.services.iam.IamRole
import software.aws.toolkits.jetbrains.services.lambda.LambdaWidgets.lambdaMemory
import software.aws.toolkits.jetbrains.services.lambda.LambdaWidgets.lambdaTimeout
import software.aws.toolkits.jetbrains.services.s3.resources.S3Resources
import software.aws.toolkits.jetbrains.ui.EnvironmentVariablesTextField
import software.aws.toolkits.jetbrains.ui.HandlerPanel
import software.aws.toolkits.jetbrains.ui.ResourceSelector
import software.aws.toolkits.jetbrains.ui.SliderPanel
import software.aws.toolkits.resources.message
import java.awt.event.ActionEvent
import java.util.Comparator
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class EditFunctionPanel internal constructor(private val project: Project) {
    lateinit var name: JTextField
    lateinit var description: JTextField
    lateinit var handlerPanel: HandlerPanel
    lateinit var createRole: JButton
    lateinit var createBucket: JButton
    lateinit var content: JPanel
    lateinit var iamRole: ResourceSelector<IamRole>
    lateinit var runtime: JComboBox<Runtime>
    lateinit var sourceBucket: ResourceSelector<Bucket>
    lateinit var envVars: EnvironmentVariablesTextField
    lateinit var deploySettings: JPanel
    lateinit var memorySlider: SliderPanel
    lateinit var timeoutSlider: SliderPanel
    lateinit var configurationSettings: JPanel
    lateinit var handlerLabel: JLabel
    lateinit var xrayEnabled: JCheckBox
    lateinit var buildSettings: JPanel
    lateinit var buildInContainer: JCheckBox

    private val runtimeModel = SortedComboBoxModel(compareBy(Comparator.naturalOrder()) { it: Runtime -> it.toString() })
    private var lastSelectedRuntime: Runtime? = null

    fun setXrayControlVisibility(visible: Boolean) {
        xrayEnabled.isVisible = visible
        if (!visible) {
            xrayEnabled.isSelected = false
        }
    }

    private fun createUIComponents() {
        handlerPanel = HandlerPanel(project)
        runtime = ComboBox(runtimeModel)
        envVars = EnvironmentVariablesTextField()
        memorySlider = lambdaMemory()
        timeoutSlider = lambdaTimeout()
        iamRole = ResourceSelector.builder(project)
            .resource(IamResources.LIST_LAMBDA_ROLES)
            .build()
        sourceBucket = ResourceSelector.builder(project)
            .resource(S3Resources.listBucketsByActiveRegion(project))
            .customRenderer { bucket, component -> component.append(bucket.name()); component }
            .build()
    }

    fun setRuntimes(runtimes: Collection<Runtime>?) {
        runtimeModel.setAll(runtimes)
    }

    init {
        deploySettings.border = IdeBorderFactory.createTitledBorder(message("lambda.upload.deployment_settings"), false)
        configurationSettings.border = IdeBorderFactory.createTitledBorder(message("lambda.upload.configuration_settings"), false)
        buildSettings.border = IdeBorderFactory.createTitledBorder(message("lambda.upload.build_settings"), false)
        runtime.addActionListener { e: ActionEvent? ->
            val index = runtime.selectedIndex
            val selectedRuntime = runtime.getItemAt(index)
            if (selectedRuntime == lastSelectedRuntime) {
                return@addActionListener
            }
            lastSelectedRuntime = selectedRuntime
            handlerPanel.setRuntime(selectedRuntime)
        }
    }
}
