import React, { useState, useEffect, Fragment } from 'react'
import { Button, message, Space, Spin, Modal } from 'antd';
import MyTable from '@/components/Table'
import MyModal from '@/components/Modal'
import { SelectWrap, InputWrap, MyButton } from '@/components/commonWrap'
import * as api from '../../services'
import { connect } from 'react-redux'
import * as actionCreators from '../store/actionCreators'
import { baseUrl2 } from '@/util/baseUrl'
import { SettingFilled } from '@ant-design/icons';
import '../../index.less'

const { CryptoJS } = window

function Index(props) {
    // 变量、常量
    const { showPage, pageCount, pageSize } = props

    // 方法
    const { newAddOrCancel, setEditInfo, setPage } = props

    const [stateList, setStateList] = useState([])
    const [taskName, settaskName] = useState('')
    const [stateCode, setStateCode] = useState('')

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
    const [taskId, setTaskId] = useState('')
    const [spinTip, setSpinTip] = useState('')
    const [clickType, setClickType] = useState('')
    const [typeCode, setTypeCode] = useState(props.typeCode)
    const [aiCode, setAiCode] = useState(null)


    useEffect(() => {
        api.getTaskTypeList({ typeCode }).then(res => {
            let { stateCodeTable } = res.data
            stateCodeTable.unshift({
                code: '',
                value: '全部'
            })
            setStateList(stateCodeTable)
        }).catch(err => { })
    }, [])

    useEffect(() => {
        initTable()
    }, [pageCount])


    useEffect(() => {
        if (aiCode) {

        }
    }, [aiCode])

    // 初始化表格
    const initTable = (page, size, stateValue, typeValue) => {
        setTableLoading(true)
        api.getTableList({
            pageCount: page ? page : pageCount,
            pageSize: size ? size : pageSize,
            typeCode,
            stateCode: stateValue ? stateValue : stateCode,
            taskName
        }).then(res => {
            const { data, titleHead, totalCount } = res
            let newColumns = [...titleHead]
            newColumns.map(item => {
                if (item.title === '序号') {
                    item.width = '70px'
                } else if (item.title === '状态') {
                    item.render = (text, record) => {
                        switch (record.stateCode) {
                            case '100000':
                                return (
                                    <div className='mds-taskMng-rowState'>
                                        <span className='state-icon wait'></span>
                                        <label>{record.stateName}</label>
                                    </div>
                                )
                            case '300000':
                            case '600000':
                            case '700000':
                            case '800000':
                                return (
                                    <div className='mds-taskMng-rowState'>
                                        <span className='state-icon rule'></span>
                                        <label>{record.stateName}</label>
                                    </div>
                                )
                            case '400000':
                                return (
                                    <div className='mds-taskMng-rowState'>
                                        <span className='state-icon end'></span>
                                        <label>{record.stateName}</label>
                                    </div>
                                )
                            case '500000':
                                return (
                                    <div className='mds-taskMng-rowState'>
                                        <span className='state-icon cancel'></span>
                                        <label>{record.stateName}</label>
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
                width: 530,
                render: (text, record) => (
                    <Space size="middle">
                        {
                            <Fragment>
                                <MyButton
                                    label='执行'
                                    disabled={record.stateCode === '400000' || record.stateCode === '500000' ? true : false}
                                    onClick={() => tableActionClick(record, 'execute')}
                                />
                                <MyButton
                                    label='导出'
                                    onClick={() => tableActionClick(record, 'export')}
                                />
                                <MyButton
                                    label={'编辑'}
                                    disabled={record.stateCode === '100000' ? false : true}
                                    onClick={() => tableActionClick(record, 'edit')}
                                    classType={'orange'}
                                />
                                <MyButton
                                    label='删除'
                                    danger
                                    disabled={record.stateCode === '100000' ? false : true}
                                    onClick={() => tableActionClick(record, 'delete')}
                                />
                            </Fragment>
                        }
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

    const getStateCodeByType = () => {
        switch (props.typeCode) {
            case 'SST':
                return '600000'
            case 'WST':
                return '700000'
            case 'EET':
            case 'RET':
                return '300000'
            case 'CCT':
            case 'MCT':
                return '800000'
        }
    }
    //表格操作事件
    const tableActionClick = (record, type) => {
        const _this = this
        switch (type) {
            case 'delete': {
                api.judgeDelete({
                    taskId: record.taskId
                }).then(res => {
                    const { data: { deleteComfirm, deleteState } } = res
                    setModalTitle('删除')
                    setDeleteState(deleteState)
                    setTaskId(record.taskId)
                    setModalVisible(true)
                    setModalContent(deleteComfirm)
                    setClickType('delete')
                }).catch(err => { })
                break;
            }
            case 'cancel': {
                setModalTitle('取消')
                setDeleteState('02')
                setTaskId(record.taskId)
                setModalVisible(true)
                setModalContent('确认取消吗？')
                setClickType('cancel')
                break;
            }
            case 'edit': {
                newAddOrCancel(2)
                setEditInfo(record)
                break;
            }
            case 'execute': {
                setSpinning(true)
                setSpinTip('数据准备中...')
                if (props.typeCode === 'SST') {
                    api.sstTaskReady({
                        taskId: record.taskId,
                        stateCode: record.stateCode,
                        fromTaskId: record.fromTaskId ? record.fromTaskId : ''
                    }).then(res => {
                        api.sstUpdateTaskState({
                            taskId: record.taskId,
                            stateCode: getStateCodeByType(),
                        }).then(res => {
                            initTable()
                            setSpinning(false)
                            window.open('#/NLPSSTPage/taskId=' + record.taskId)
                        }).catch(err => {
                            setSpinning(false)
                        })
                    }).catch(err => {
                        setSpinning(false)
                    })
                } else if (props.typeCode === 'WST') {
                    api.wstTaskReady({
                        taskId: record.taskId,
                        stateCode: record.stateCode,
                        fromTaskId: record.fromTaskId ? record.fromTaskId : ''
                    }).then(res => {
                        api.sstUpdateTaskState({
                            taskId: record.taskId,
                            stateCode: getStateCodeByType(),
                        }).then(res => {
                            initTable()
                            setSpinning(false)
                            window.open('#/WSTPage/taskId=' + record.taskId)
                        }).catch(err => {
                            setSpinning(false)
                        })
                    }).catch(err => {
                        setSpinning(false)
                    })
                } else if (props.typeCode === 'EET') {
                    api.eetTaskReady({
                        taskId: record.taskId,
                        stateCode: record.stateCode,
                        fromTaskId: record.fromTaskId ? record.fromTaskId : ''
                    }).then(res => {
                        api.sstUpdateTaskState({
                            taskId: record.taskId,
                            stateCode: getStateCodeByType(),
                        }).then(res => {
                            initTable()
                            setSpinning(false)
                            window.open('#/EETPage/taskId=' + record.taskId)
                        }).catch(err => {
                            setSpinning(false)
                        })
                    }).catch(err => {
                        setSpinning(false)
                    })
                } else if (props.typeCode === 'RET') {
                    api.retTaskReady({
                        taskId: record.taskId,
                        stateCode: record.stateCode,
                        fromTaskId: record.fromTaskId ? record.fromTaskId : ''
                    }).then(res => {
                        api.sstUpdateTaskState({
                            taskId: record.taskId,
                            stateCode: getStateCodeByType(),
                        }).then(res => {
                            initTable()
                            setSpinning(false)
                            window.open('#/RETPage/taskId=' + record.taskId)
                        }).catch(err => {
                            setSpinning(false)
                        })
                    }).catch(err => {
                        setSpinning(false)
                    })
                } else if (props.typeCode === 'CCT') {
                    api.cctTaskReady({
                        taskId: record.taskId,
                        stateCode: record.stateCode,
                        fromTaskId: record.fromTaskId ? record.fromTaskId : ''
                    }).then(res => {
                        api.sstUpdateTaskState({
                            taskId: record.taskId,
                            stateCode: getStateCodeByType(),
                        }).then(res => {
                            initTable()
                            setSpinning(false)
                            window.open('#/CCTPage/taskId=' + record.taskId + ',fromTaskId=' + (record.fromTaskId ? 'true' : 'false'))
                        }).catch(err => {
                            setSpinning(false)
                        })
                    }).catch(err => {
                        setSpinning(false)
                    })
                } else if (props.typeCode === 'MCT') {
                    api.mctTaskReady({
                        taskId: record.taskId,
                        stateCode: record.stateCode,
                        fromTaskId: record.fromTaskId ? record.fromTaskId : ''
                    }).then(res => {
                        api.sstUpdateTaskState({
                            taskId: record.taskId,
                            stateCode: getStateCodeByType(),
                        }).then(res => {
                            initTable()
                            setSpinning(false)
                            window.open('#/MCTPage/taskId=' + record.taskId + ',fromTaskId=' + (record.fromTaskId ? 'true' : 'false'))
                        }).catch(err => {
                            setSpinning(false)
                        })
                    }).catch(err => {
                        setSpinning(false)
                    })
                }

                break;
            }
            case 'check': {
                if (record.stateCode === '200000') {
                    if (record.typeCode === '02') {
                        switch (record.litleStateCode) {
                            case '200204':
                                window.open('#/SchemaCheckOut/taskId=' + record.taskId)
                                break;
                            case '200203':
                                window.open('#/UsMakeRulesPage/taskId=' + record.taskId)
                                break;
                        }
                    } else {
                        window.open('#/MakeRulesPage/taskId=' + record.taskId)
                    }
                } else if (record.stateCode === '300000') {
                    switch (record.litleStateCode) {
                        case '300102':
                            window.open('#/StructuredKnowlMark/taskId=' + record.taskId)
                            break;
                        case '300202':
                            window.open('#/UsMarkTrainingCheck/taskId=' + record.taskId)
                            break;
                        case '300204':
                            window.open('#/UsResultAcceptance/taskId=' + record.taskId)
                            break;
                    }
                }
                break;
            }
            case 'mark': {
                async function openFun(params) {
                    setSpinning(true)
                    setSpinTip('任务准备中...')
                    const res = await readyTask(record)
                    if (res) {
                        window.open('#/MarkingTaskPage/taskId=' + record.taskId)
                    }
                    return res
                }
                openFun()
                break;
            }
            case 'export': {
                window.location.href = baseUrl2 + '/nlp/common/download/files?taskId=' + record.taskId
                break;
            }
        }
    }

    const readyTask = (record) => {
        return new Promise(resolve => {
            api.readyTask({
                taskId: record.taskId,
                stateCode: record.stateCode
            }).then(res => {
                if (record.stateCode === '100000') {
                    api.updateTaskStatu({
                        taskId: record.taskId,
                        stateCode: '300000'
                    }).then(res => {
                        initTable()
                        setSpinning(false)
                        resolve(true)
                    }).catch(err => { })
                } else {
                    setSpinning(false)
                    resolve(true)
                }
            }).catch(err => {
            })
        })

    }

    // 弹窗确认事件
    const modalOk = () => {
        setModalVisible(false)
    }

    // 弹窗确认操作事件
    const confirmClick = () => {
        setModalVisible(false)
        setSpinning(true)
        switch (clickType) {
            case 'delete': {
                setSpinTip('删除中...')
                api.comfirmDelete({
                    taskId
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
                break;
            }
            case 'cancel': {
                setSpinTip('操作中...')
                api.sstUpdateTaskState({
                    taskId,
                    stateCode: '500000'
                }).then(res => {
                    setSpinning(false)
                    initTable()
                    Modal.success({
                        content: '操作成功',
                    });
                }).catch(err => {
                    message.destroy()
                    Modal.error({
                        content: '操作失败',
                    });
                    setSpinning(false)
                })
                break;
            }
            case 'ai': {
                if (aiCode) {
                    setSpinTip('标注中...')
                    api.trainingModel({
                        taskId,
                        modelId: aiCode
                    }).then(res => {
                        setSpinning(false)
                        initTable()
                    }).catch(err => {

                    })
                } else {
                    setSpinning(false)
                    message.error('请选择模型！')
                }

                break;
            }
        }


    }

    // 类型选择框事件
    const selChange = (value, type) => {
        if (type === 'state') {
            setStateCode(value)
            initTable(1, 10, value ? value : 'all', typeCode)
        } else if (type === 'ai') {
            setAiCode(value)
        }
        setPage(1, 10)


    }

    // 输入框事件
    const inputChange = ({ target: { value } }) => {
        settaskName(value)
    }

    return (
        <Fragment>
            <div className="mds-actionsWrap">
                <Button size='large' type='primary' onClick={() => newAddOrCancel(2)}>
                    {'创建任务'}
                </Button>
                <div className='mds-conditionWrap'>
                    {/* <SelectWrap style={{ width: '150px' }} label='状态' size='large' list={stateList} selChange={(value) => selChange(value, 'state')} /> */}
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
                    pagination={true}
                    pageSize={pageSize}
                    pageChange={pageChange}
                    total={pageTotal}
                />
            </Spin>
            <MyModal
                visible={modalVisible}
                modalOk={modalOk}
                modalCancel={() => {
                    setModalVisible(false)
                }}
                title={modalTitle}
                footer={
                    deleteState === '01'
                        ?
                        <Button type={'primary'} onClick={() => {
                            setModalVisible(false)
                        }}>确定</Button>
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
                    <p>{modalContent}</p>
                </Fragment>
            </MyModal>
        </Fragment>
    )
}

const mapStateToProps = (state) => {
    return { ...state.NLPTaskTaskReducer }
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
        },
        setTypeCode(value) {
            const action = actionCreators.setTypeCode(value)
            dispatch(action)
        }
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(Index)