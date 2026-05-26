import React, { Fragment, useState, useEffect } from 'react'
import { SelectWrap, InputWrap, MyButton } from '@/components/commonWrap'
import { Space, Spin } from 'antd'
import MyTable from '@/components/Table'
import * as api from './services'
import { connect } from 'react-redux'
import * as actionCreators from './store/actionCreators'
import './index.less'

function Index(props) {
    
    const [taskTypeStateList, setTaskTypeStateList] = useState([])
    const [spinning, setSpinning] = useState(false)
    const [spinTip, setsSpinTip] = useState('')
    const [dataSource, setDataSource] = useState([])
    const [columns, setColumns] = useState([])
    const [tableLoading, setTableLoading] = useState(true)
    const [pageCount, setPageCount] = useState(1)
    const [pageSize, setPageSize] = useState(10)
    const [pageTotal, setPageTotal] = useState(0)
    const [typeCode, setTypeCode] = useState(props.typeCode)
    const [stateCode, setStateCode] = useState(null)
    const [taskName, setTaskName] = useState()

    useEffect(() => {
        initTable()
        initSelList()
    }, [])

    useEffect(() => {
        if (stateCode !== null) {
            initTable()
        }
    }, [stateCode])

    useEffect(() => {
        if (props.isInit) {
        }
    }, [props.isInit])

    const initSelList = () => {
        api.getSelList({typeCode}).then(res => {
            const { SemiStructureCodeTable, StructureCodeTable, UnStructureCodeTable, typeCodeTable } = res.data
            SemiStructureCodeTable.unshift({
                value: '全部', code: 'all'
            })
            StructureCodeTable.unshift({
                value: '全部', code: 'all'
            })
            UnStructureCodeTable.unshift({
                value: '全部', code: 'all'
            })
            typeCodeTable.unshift({
                value: '全部', code: 'all'
            })
            setTaskTypeStateList(StructureCodeTable)
        }).catch(err => { })
    }

    const initTable = (count) => {
        setTableLoading(true)
        api.getTableList({
            pageSize,
            pageCount: count ? count : pageCount,
            typeCode,
            stateCode,
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
                            case '200102':
                            case '300102':
                            case '200203':
                            case '200204':
                            case '300202':
                            case '300204':
                            case '100101':
                            case '100201':
                                return (
                                    <div className='mds-taskMng-rowState'>
                                        <span className='state-icon label'></span>
                                        <label>{record.stateName}</label>
                                    </div>
                                )
                            case '200101':
                            case '300101':
                            case '200201':
                            case '200202':
                            case '300201':
                            case '300203':
                                return (
                                    <div className='mds-taskMng-rowState'>
                                        <span className='state-icon rule'></span>
                                        <label>{record.stateName}</label>
                                    </div>
                                )
                            case '400201':
                            case '400101':
                            case '500101':
                            case '500201':
                                return (
                                    <div className='mds-taskMng-rowState'>
                                        <span className='state-icon end'></span>
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
                width: 270,
                render: (text, record) => (
                    <Space size="middle">
                        <MyButton
                            label='执行'
                            onClick={() => tableActionClick(record)}
                            disabled={record.exeClick === '00' ? false : true}
                        />
                    </Space>
                ),
            })
            setColumns(newColumns)
            setDataSource(data)
            setTableLoading(false)
            setPageTotal(totalCount)
        }).catch(err => {
            setTableLoading(false)
        })
    }

    const tableActionClick = (record) => {
        const { stateCode, typeCode } = record
        switch (stateCode) {
            case '200101': {
                window.open('#/MappingRulesPage/taskId=' + record.taskId)
                break;
            }
            case '200201': {
                window.open('#/UsMappingRulesPage/taskId=' + record.taskId)
                break;
            }
            case '200202': {
                window.open('#/LabelDefinition/taskId=' + record.taskId)
                break;
            }
            case '300101': {
                window.open('#/ChartsAttach/taskId=' + record.taskId)
                break;
            }
            case '300201': {
                window.open('#/UsMarkingTraining/taskId=' + record.taskId)
                break;
            }
            case '300203': {
                window.open('#/FormalMark/taskId=' + record.taskId)
                break;
            }
        }
    }

    const selChange = (value, type) => {
        setStateCode(value)
    }

    const inputChange = ({ target: { value } }) => {
        setTaskName(value)
    }

    const pageChange = (count, size) => {
        setPageCount(count)
        setPageSize(size)
        initTable(count)
    }

    return (
        <Fragment>
            <div className="mds-actionsWrap" style={{ justifyContent: 'flex-end' }}>
                <div className='mds-conditionWrap'>
                    <SelectWrap
                        style={{ width: '200px' }}
                        label='任务状态' size='large'
                        list={taskTypeStateList}
                        selChange={(value) => selChange(value, 'state')}
                    />
                    <InputWrap label='名称' size='large' inputChange={inputChange} />
                    <MyButton
                        label='搜索'
                        size='large'
                        onClick={() => pageChange(1, 10)}
                    />
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
        </Fragment>
    )
}


const mapStateToProps = (state) => {
    return { ...state.TaskExeReducer }
}

const mapDispatchToProps = (dispatch) => {
    return {
        initTaskExecution() {
            const action = actionCreators.initTaskExecution(false)
            dispatch(action)
        },
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(Index)