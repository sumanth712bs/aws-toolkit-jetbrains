// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0
package software.aws.toolkits.jetbrains.services.cloudformation.stack

import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import javax.swing.JComponent
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer

class DynamicTableView<T>(private vararg val fields: Field<T>) : View {
    private val model = object : DefaultTableModel(fields.map(Field<T>::readableName).toTypedArray(), 0) {
        override fun isCellEditable(row: Int, column: Int) = false
    }

    private val table = JBTable(model).apply {
        autoCreateRowSorter = true
        autoscrolls = true
        setShowColumns(true)
        setPaintBusy(true)
        fields.forEach { field ->
            field.renderer?.let {
                getColumn(field.readableName).cellRenderer = it
            }
        }
    }

    override val component: JComponent = JBScrollPane(table)

    fun updateItems(items: List<T>, clearExisting: Boolean = false) {
        assert(SwingUtilities.isEventDispatchThread())
        if (clearExisting) {
            model.rowCount = 0
        }
        items.forEachIndexed { index, item ->
            model.insertRow(index, fields.map { it.getData(item) }.toTypedArray())
        }
        table.setPaintBusy(false)
    }

    fun showBusy(busy: Boolean = true) {
        table.setPaintBusy(busy)
    }

    data class Field<T>(
        val readableName: String,
        val renderer: TableCellRenderer? = null,
        val getData: (T) -> Any?
    )
}
