import React from 'react'
import { Table } from 'antd'
import './index.less'

export default function index(props) {
    const { dataSource, columns, loading, pageCount, pageSize, total } = props
    const { pageChange } = props
    return (
        <div className='mds-table-wrap'>
            <Table
                rowKey="rowKey"
                loading={loading}
                scroll={props.scroll ? props.scroll : { x: 1500 }}
                dataSource={dataSource}
                columns={columns}
                pagination={props.pagination ? {
                    current: pageCount,
                    pageSize,
                    onChange: pageChange,
                    total
                } : false}
            />
        </div>
    )
}
