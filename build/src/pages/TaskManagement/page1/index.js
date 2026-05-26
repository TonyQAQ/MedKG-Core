import React, { useState, useEffect, Fragment } from 'react'
import { Button, message, Space, Spin, Modal } from 'antd';
import MyTable from '@/components/Table'
import MyModal from '@/components/Modal'
import { SelectWrap, InputWrap, MyButton } from '@/components/commonWrap'
import * as api from '../services'
import { connect } from 'react-redux'
import * as actionCreators from '../store/actionCreators'
import { baseUrl2 } from '@/util/baseUrl'
import { SettingFilled } from '@ant-design/icons';
import Axios from 'axios'
import '../index.less'

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
    const [modalVisible2, setModalVisible2] = useState(false)
    const [modalTitle, setModalTitle] = useState('')
    const [deleteState, setDeleteState] = useState('')
    const [modalContent, setModalContent] = useState('')
    const [taskId, setTaskId] = useState('')
    const [spinTip, setSpinTip] = useState('')
    const [clickType, setClickType] = useState('')
    const [typeCode, setTypeCode] = useState(props.typeCode)
    const [aiList, setAiList] = useState([])
    const [aiCode, setAiCode] = useState(null)


    useEffect(() => {
        api.getTaskTypeList({ typeCode }).then(res => {
            let { stateCodeTable } = res.data
            stateCodeTable.unshift({
                code: '',
                value: '全部'
            })
            if (typeCode === '04') {
                stateCodeTable.map((item, index) => {
                    if (item.value === '规范制定') {
                        stateCodeTable.splice(index, 1)
                    }
                })
            }
            setStateList(stateCodeTable)
        }).catch(err => { })
        initAiList()
    }, [])

    useEffect(() => {
        if (typeCode) {
            initTable()
        }
    }, [typeCode])


    useEffect(() => {
        if (aiCode) {

        }
    }, [aiCode])

    // 初始化模型列表
    const initAiList = () => {
        api.initAiList().then(res => {
            setAiList(res.data)
        })
    }

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
                            case '200000':
                                return (
                                    <div className='mds-taskMng-rowState'>
                                        <span className='state-icon rule'></span>
                                        <label>{record.stateName}</label>
                                    </div>
                                )
                            case '300000':
                                return (
                                    <div className='mds-taskMng-rowState'>
                                        <span className='state-icon label'></span>
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
                            typeCode === '04'
                                ?
                                <Fragment>
                                    <MyButton
                                        label='标注'
                                        // disabled={record.exeState === '1000' ? true : (record.stateCode !== '400000' && record.stateCode !== '500000' ? false : true)}
                                        // disabled={record.exeState === '1000' ? true : false}
                                        onClick={() => tableActionClick(record, 'mark')}
                                    />
                                    <MyButton
                                        label={<div>{record.exeState === '1000' ? <SettingFilled spin /> : null}{record.exeState === '0000' || record.exeState === '2000' ? '智能标注' : (record.exeState === '1000' ? '标注中' : '标注结束')}</div>}
                                        // label={<div>{record.exeState === '1000' ? <SettingFilled spin /> : null}{record.exeState === '0000' ? '智能标注' : (record.exeState === '1000' ? '标注中' : '标注结束')}</div>}
                                        // disabled={record.exeState === '0000' || record.exeState === '1000' ? false : true}
                                        // onClick={record.exeState === '0000' ? () => tableActionClick(record, 'aiMark') : null}
                                        onClick={() => tableActionClick(record, 'aiMark')}
                                        classType={record.exeState === '0000' ? null : (record.exeState === '1000' ? 'green' : null)}
                                    />
                                    <MyButton
                                        label='导出'
                                        classType='orange'
                                        disabled={record.stateCode === '400000' ? false : true}
                                        onClick={() => tableActionClick(record, 'export')}
                                    />
                                </Fragment>
                                :
                                <Fragment>
                                    <MyButton
                                        label='执行'
                                        disabled={record.stateCode === '100000' ? false : true}
                                        onClick={() => tableActionClick(record, 'execute')}
                                    />
                                    <MyButton
                                        label='校验'
                                        disabled={record.checkState === '00' ? false : true}
                                        classType='green'
                                        onClick={() => tableActionClick(record, 'check')}
                                    />
                                </Fragment>
                        }

                        <MyButton
                            label='编辑'
                            disabled={record.stateCode === '100000' ? false : true}
                            classType='orange'
                            onClick={() => tableActionClick(record, 'edit')}
                        />
                        <MyButton
                            label='取消'
                            disabled={
                                typeCode === '04'
                                    ?
                                    record.stateCode !== '400000' && record.stateCode !== '500000' ? false : true
                                    :
                                    record.stateCode !== '100000' && record.stateCode !== '400000' && record.stateCode !== '500000' ? false : true
                            }
                            classType='warm'
                            onClick={() => tableActionClick(record, 'cancel')}
                        />
                        <MyButton
                            label='删除'
                            disabled={typeCode === '04' ? (record.stateCode !== '400000' ? false : true) : (record.stateCode !== '100000' && record.stateCode !== '500000' ? true : false)}
                            danger
                            onClick={() => tableActionClick(record, 'delete')}
                        />
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
                api.judgeCancel({
                    taskId: record.taskId
                }).then(res => {
                    const { data: { deleteComfirm, deleteState } } = res
                    setModalTitle('取消')
                    setDeleteState(deleteState)
                    setTaskId(record.taskId)
                    setModalVisible(true)
                    setModalContent(deleteComfirm)
                    setClickType('cancel')
                }).catch(err => { })
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
                api.taskReady({
                    taskId: record.taskId
                }).then(res => {
                    initTable()
                    setSpinning(false)
                    window.open('#/TaskMngExecutePage/taskId=' + record.taskId)
                }).catch(err => {
                    setSpinning(false)
                })
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
                window.location.href = baseUrl2 + '/task/us/ql/download/files?taskId=' + record.taskId + '&taskName=' + record.taskName
                break;
            }
            case 'aiMark': {
                setModalVisible2(true)
                setModalTitle('选择模型')
                setClickType('ai')
                setTaskId(record.taskId)
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
                api.comfirmCancel({
                    taskId
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
                    if (aiCode === '001') {
                        Axios.post(`http://172.18.194.203:9090/api/v1/ner?taskId=${taskId}`, {
                            modelId: aiCode
                        }).then(res => {

                        }).catch(err => {

                        })
                        setTimeout(() => {
                            setModalVisible2(false)
                            setSpinning(false)
                            initTable()
                        }, 3000);
                    } else {
                        Axios.post(`http://172.18.194.203:9090/api/v1/relation?taskId=${taskId}`, {
                            modelId: aiCode
                        }).then(res => {

                        }).catch(err => {

                        })
                        setTimeout(() => {
                            setModalVisible2(false)
                            setSpinning(false)
                            initTable()
                        }, 3000);
                    }
                    // api.trainingModel({
                    //     taskId,
                    //     modelId: aiCode
                    // }).then(res => {
                    //     setModalVisible2(false)
                    //     setSpinning(false)
                    //     initTable()
                    // }).catch(err => {

                    // })
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
                    <SelectWrap style={{ width: '150px' }} label='状态' size='large' list={stateList} selChange={(value) => selChange(value, 'state')} />
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
            <MyModal
                visible={modalVisible2}
                modalOk={modalOk}
                modalCancel={() => {
                    setModalVisible2(false)
                }}
                title={modalTitle}
                footer={
                    <Fragment>
                        <Button type={'primary'} onClick={confirmClick}>确定</Button>
                        <MyButton label='取消' classType='warm' onClick={() => {
                            setModalVisible2(false)
                        }} />
                    </Fragment>
                }
            >
                <Fragment>
                    <SelectWrap style={{ width: '150px' }} label='模型列表' size='large' list={aiList} selChange={(value) => selChange(value, 'ai')} />
                </Fragment>
            </MyModal>
        </Fragment>
    )
}

const mapStateToProps = (state) => {
    return { ...state.TaskMngReducer }
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