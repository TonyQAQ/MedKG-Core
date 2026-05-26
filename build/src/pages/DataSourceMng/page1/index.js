import React, { useState, useEffect, Fragment } from 'react'
import { Button, message, Space, Spin, Modal } from 'antd';
import MyTable from '@/components/Table'
import MyModal from '@/components/Modal'
import { SelectWrap, InputWrap, MyButton } from '@/components/commonWrap'
import * as api from '../services'
import { connect } from 'react-redux'
import * as actionCreators from '../store/actionCreators'

function Index(props) {

    // 变量、常量
    const { showPage, pageCount, pageSize } = props

    // 方法
    const { newAddOrCancel, setEditInfo, setPage } = props

    const [dataSource, setDataSource] = useState([])
    const [columns, setColumns] = useState([])
    const [tableLoading, setTableLoading] = useState(true)
    // const [pageCount, setPageCount] = useState(pageCount)
    // const [pageSize, setPageSize] = useState(pageSize)
    const [pageTotal, setPageTotal] = useState(0)
    const [spinning, setSpinning] = useState(false)
    const [modalVisible, setModalVisible] = useState(false)
    const [modalTitle, setModalTitle] = useState('')
    const [deleteState, setDeleteState] = useState('')
    const [modalContent, setModalContent] = useState('')
    const [connectionId, setConnectionId] = useState('')
    const [spinTip, setSpinTip] = useState('')
    const [dataTypeInfoList, setDataTypeInfoList] = useState([])
    const [databaseType, setDatabaseType] = useState('')
    const [connectionName, setConnectionName] = useState('')
    const [clickType, setClickType] = useState('')

    useEffect(() => {
        api.getSourceTypeInfo().then(res => {
            let list = res.data
            list.unshift({
                code: '',
                value: '全部'
            })
            setDataTypeInfoList(list)
        }).catch(err => { })
        initTable()
    }, [])

    // 初始化表格
    const initTable = (page, size, selValue) => {
        setTableLoading(true)
        api.getDataSourceList({
            pageCount: page ? page : pageCount,
            pageSize: size ? size : pageSize,
            databaseType: selValue ? selValue : databaseType,
            connectionName
        }).then(res => {
            const { data, titleHead, totalCount } = res
            let newColumns = [...titleHead]
            newColumns.map(item => {
                if (item.title === '序号') {
                    item.width = '70px'
                }
            })
            newColumns.push({
                title: "操作",
                key: 'action',
                fixed: 'right',
                width: 270,
                render: (text, record) => (
                    <Space size="middle">
                        <MyButton label='测试' classType='green' onClick={() => tableActionClick(record, 'testLink')} />
                        <MyButton label='编辑' classType='orange' onClick={() => tableActionClick(record, 'edit')} />
                        <MyButton disabled={record.databaseState === '01' ? true : false} label='删除' danger onClick={() => tableActionClick(record, 'delete')} />
                    </Space>
                ),
            })
            setDataSource(data)
            setColumns(newColumns)
            setPageTotal(totalCount)
            setTableLoading(false)
        }).catch(err => {
            setTableLoading(false)
        })
    }
    // 翻页
    const pageChange = (pageCount, pageSize) => {
        setPage(pageCount, pageSize)
        initTable(pageCount, pageSize)
    }
    //表格操作事件
    const tableActionClick = (record, type) => {
        const _this = this
        switch (type) {
            case 'testLink': {
                setSpinning(true)
                setSpinTip('测试连接中...')
                api.testLink({
                    connectionId: record.connectionId
                }).then(res => {
                    setSpinning(false)
                    message.success('连接成功')
                }).catch(err => {
                    setSpinning(false)
                })
                break;
            }
            case 'delete': {
                api.getSourceJudgeDeleteInfo({
                    connectionId: record.connectionId
                }).then(res => {
                    const { data: { deleteComfirm, deleteState } } = res
                    setModalTitle('删除')
                    setDeleteState(deleteState)
                    setModalVisible(true)
                    setConnectionId(record.connectionId)
                    setModalContent(deleteComfirm)
                    setClickType('delete')
                }).catch(err => { })
                break;
            }
            case 'edit': {
                api.getSourceJudgeEditInfo({
                    connectionId: record.connectionId
                }).then(res => {
                    const { data: { updateComfirm, updateState } } = res
                    setEditInfo(record)
                    if (updateState === '02') {
                        newAddOrCancel(2)
                    } else {
                        setModalTitle('编辑')
                        setDeleteState(updateState)
                        setModalVisible(true)
                        setConnectionId(record.connectionId)
                        setModalContent(updateComfirm)
                        setClickType('edit')
                    }
                }).catch(err => { })
            }
        }
    }

    // 弹窗确认事件
    const modalOk = () => {
        setModalVisible(false)
    }

    // 确认删除事件
    const confirmClick = () => {
        if (clickType === 'delete') {
            setModalVisible(false)
            setSpinning(true)
            setSpinTip('删除中...')
            api.dataSourceMngDelete({
                connectionId
            }).then(res => {
                setSpinning(false)
                initTable()
                Modal.success({
                    content: '删除成功',
                });
            }).catch(err => {
                message.destroy()
                Modal.error({
                    content: '删除失败',
                });
                setSpinning(false)
            })
        } else {
            newAddOrCancel(2)
        }

    }

    // 类型选择框事件
    const selChange = (value) => {
        setPage(1, 10)
        setDatabaseType(value)
        initTable(1, 10, value ? value : 'all')
    }

    // 输入框事件
    const inputChange = ({ target: { value } }) => {
        setConnectionName(value)
    }

    return (
        <Fragment>
            <div className="mds-actionsWrap">
                <Button size='large' type='primary' onClick={() => newAddOrCancel(2)}>
                    {'创建数据源'}
                </Button>
                <div className='mds-conditionWrap'>
                    <SelectWrap label='类型' size='large' list={dataTypeInfoList} selChange={selChange} />
                    <InputWrap label='名称' size='large' inputChange={inputChange} />
                    <Button size='large' type='primary' onClick={() => initTable()}>
                        {'搜索'}
                    </Button>
                </div>
            </div>
            <Spin spinning={spinning} tip={spinTip}>
                <MyTable
                    dataSource={dataSource}
                    columns={columns}
                    loading={tableLoading}
                    pageCount={pageCount}
                    pageSize={pageSize}
                    pagination={true}
                    pageChange={pageChange}
                    total={pageTotal}
                />
            </Spin>
            <MyModal
                visible={modalVisible}
                // modalOk={modalOk}
                modalCancel={() => {
                    setModalVisible(false)
                }}
                title={modalTitle}
                footer={
                    deleteState === '01'
                        ?
                        <Button type={'primary'} onClick={() => {
                            setModalVisible(false)
                        }}>
                            {'确定'}
                        </Button>
                        :
                        <Fragment>
                            <Button type={'primary'} onClick={confirmClick}>确定</Button>
                            <MyButton label='取消' classType='warm' onClick={() => {
                                setModalVisible(false)
                            }} />
                        </Fragment>

                }
            >
                <Fragment>
                    <p dangerouslySetInnerHTML={{ __html: modalContent }}></p>
                </Fragment>
            </MyModal>
        </Fragment>
    )
}

const mapStateToProps = (state) => {
    return { ...state.DataSourceMngReducer }
}

const mapDispatchToProps = (dispatch) => {
    return {
        newAddOrCancel(showPage) {
            const action = actionCreators.newAddOrCancel(showPage)
            dispatch(action)
        },
        setEditInfo(info) {
            const action = actionCreators.setEditInfo(info)
            dispatch(action)
        },
        setPage(page, pageSize) {
            const action = actionCreators.setPage(page, pageSize)
            dispatch(action)
        }
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(Index)