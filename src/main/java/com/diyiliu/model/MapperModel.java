package com.diyiliu.model;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: MapperModel
 * Author: DIYILIU
 * Update: 2017-08-16 14:52
 */
public class MapperModel extends AbstractTableModel {

    private final String[] columnNames = new String[]{"序号", "协议", "内网地址", "内网端口", "外网地址", "外网端口"};

    private List<Pair> pairs;
    private List data = new ArrayList();

    public MapperModel() {

    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = ((Object[]) data.get(rowIndex))[columnIndex];

        return value;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public void refresh() {

        this.fireTableDataChanged();
    }

    public void refresh(List<Pair> list) {
        pairs = list;
        data = toListArray(pairs);
        this.fireTableDataChanged();
    }

    public Object[] getRowData(int rowIndex) {

        return (Object[]) data.get(rowIndex);
    }

    public List<Pair> getPairs() {
        return pairs;
    }

    /**
     * 把Pair对象数组，转为Array对象数组
     *
     * @param l
     * @return
     */
    public List toListArray(List<Pair> l) {

        List list = new ArrayList();
        for (int i = 0; i < l.size(); i++) {
            Pair p = l.get(i);
            list.add(p.toArray(i + 1));
        }

        return list;
    }
}