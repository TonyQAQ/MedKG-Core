import React, { useState, useEffect, Fragment } from 'react'
import { Button, Space, Spin } from 'antd';
import MyTable from '@/components/Table'
import MyModal from '@/components/Modal'
import { SelectWrap, InputWrap, MyButton } from '@/components/commonWrap'
import * as api from './services'
import { baseUrl3 } from '@/util/baseUrl'
import '../TaskManagement/index.less'

export default function Index(props) {


    const [dataSource, setDataSource] = useState([])
    const [columns, setColumns] = useState([])
    const [tableLoading, setTableLoading] = useState(true)
    const [spinning, setSpinning] = useState(false)
    const [modalVisible, setModalVisible] = useState(false)
    const [modalTitle, setModalTitle] = useState('')
    const [deleteState, setDeleteState] = useState('')
    const [modalContent, setModalContent] = useState('')
    const [spinTip, setSpinTip] = useState('')
    const [keyWord, setKeyWord] = useState(null)
    const [clickType, setClickType] = useState('')
    const [selList, setSelList] = useState([])
    const [stateCode, setStateCode] = useState(null)
    const [pageCount, setPageCount] = useState(1)
    const [pageSize, setPageSize] = useState(10)
    const [pageTotal, setPageTotal] = useState(0)
    const [taskId, setTaskId] = useState('')
    const [typeCode, setTypeCode] = useState(null)


    useEffect(() => {
        initSelList()
        initTable()
    }, [])

    useEffect(() => {
        if (stateCode) {
            initTable()
        }
    }, [stateCode, keyWord])

    // 初始化下拉框
    const initSelList = () => {
        api.getSelList().then(res => {
            const { stateCodeTable } = res.data
            stateCodeTable.unshift({
                value: "全部", code: "all"
            })
            setSelList(stateCodeTable)
        }).catch(err => { })
    }

    // 初始化表格
    const initTable = (page, size) => {
        setTableLoading(true)
        api.getTableList({
            pageCount: page ? page : pageCount,
            pageSize: size ? size : pageSize,
            stateCode,
            keyWord
        }).then(res => {
            const { data, titleHead, totalCount } = res
            let newColumns = [...titleHead]
            newColumns.map(item => {
                if (item.title === '序号') {
                    item.width = '70px'
                } else if (item.title === '状态') {
                    item.render = (text, record) => {
                        switch (record.mapStateName) {
                            case '待生成':
                                return (
                                    <div className='mds-taskMng-rowState'>
                                        <span className='state-icon wait'></span>
                                        <label>{record.mapStateName}</label>
                                    </div>
                                )
                            case '已完成':
                                return (
                                    <div className='mds-taskMng-rowState'>
                                        <span className='state-icon rule'></span>
                                        <label>{record.mapStateName}</label>
                                    </div>
                                )
                            case '生成中':
                                return (
                                    <div className='mds-taskMng-rowState'>
                                        <span className='state-icon label'></span>
                                        <label>{record.mapStateName}</label>
                                    </div>
                                )
                        }
                    }
                }
            })
            newColumns.push({
                title: "操作",
                key: 'action',
                fixed: 'right',
                width: 350,
                render: (text, record) => (
                    <Space size="middle">
                        <MyButton disabled={record.mapStateCode === '1000' ? false : true} label='生成' onClick={() => tableActionClick(record, 'create')} />
                        <MyButton disabled={record.mapStateCode === '3000' ? false : true} label='导出' classType='orange' onClick={() => tableActionClick(record, 'export')} />
                        <MyButton disabled={record.mapStateCode === '3000' ? false : true} label='删除' danger onClick={() => tableActionClick(record, 'delete')} />
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
        setPageCount(pageCount)
        initTable(pageCount, pageSize)
    }
    //表格操作事件
    const tableActionClick = (record, type) => {
        switch (type) {
            case 'delete': {
                // api.getSourceJudgeDeleteInfo({
                //     connectionId: record.connectionId
                // }).then(res => {
                //     const { data: { deleteComfirm, deleteState } } = res
                //     setModalTitle('删除')
                //     setDeleteState(deleteState)
                //     setModalVisible(true)
                //     setConnectionId(record.connectionId)
                //     setModalContent(deleteComfirm)
                //     setClickType('delete')
                // }).catch(err => { })
                break;
            }
            case 'create': {
                switch (record.mapStateName) {
                    case '待生成': {
                        setModalTitle('提示')
                        setModalVisible(true)
                        setModalContent('确认开始生成图谱吗？')
                        setClickType('create')
                        setTaskId(record.taskId)
                        setTypeCode(record.typeCode)
                        break;
                    }
                }
                break;
            }
            case 'export': {
                window.location.href = baseUrl3 + '/knowledge/export?taskId=' + record.taskId
                break;
            }
        }
    }

    // 确认删除事件
    const confirmClick = () => {
        if (clickType === 'delete') {
            setModalVisible(false)
            setSpinning(true)
            setSpinTip('删除中...')
            // api.dataSourceMngDelete({
            //     connectionId
            // }).then(res => {
            //     setSpinning(false)
            //     initTable()
            //     Modal.success({
            //         content: '删除成功',
            //     });
            // }).catch(err => {
            //     message.destroy()
            //     Modal.error({
            //         content: '删除失败',
            //     });
            //     setSpinning(false)
            // })
        } else if (clickType === 'create') {
            api.createAtlas({
                taskId,
                type: typeCode
            }).then(res => {

            }).catch(err => { })
            setModalVisible(false)
            setSpinning(true)
            setSpinTip('生成中...')
            setTimeout(() => {
                setSpinning(false)
                initTable()
            }, 3000);
        }
    }

    // 类型选择框事件
    const selChange = (value) => {
        setStateCode(value)
    }

    // 输入框事件
    const inputChange = ({ target: { value } }) => {
        setKeyWord(value)
    }

    return (
        <Fragment>
            <div className="mds-actionsWrap" style={{ justifyContent: 'flex-end' }}>
                <div className='mds-conditionWrap'>
                    <SelectWrap label='类型' size='large' list={selList} selChange={selChange} />
                    <InputWrap label='任务名称' size='large' inputChange={inputChange} />
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

