import React, { Fragment, Component } from 'react'
import { Input, Select, Space, Tooltip, Button, Spin, message, AutoComplete, Switch, Empty } from 'antd';
import { SelectWrap, InputWrap, MyButton } from '@/components/commonWrap'
import LayoutHeader from '@/components/Header'
import MyTable from '@/components/Table'
import * as api from './services'
import debounce from 'lodash/debounce';
import MyModal from '@/components/Modal'
import 'rc-color-picker/assets/index.css';
import '../index.less'

const { Option } = Select

export default class Index extends Component {
    constructor(props) {
        super(props)
        this.state = {
            pageCount: 1,
            pageSize: 10,
            pageTotal: 0,
            liList: [],
            taskId: '',
            dataSource: [],
            columns: [],
            titleHead: [],
            currentSelectIndex: 0,
            selVal: '',
            keyWord: '',
            tableId: '',
            tableLoading: true,
            spinning: false,
            tableSelVal: '',
            tableKeyWord: '',
            modalVisible: false,
            modalContent: '',
            publishState: '',
            modalTitle: '',
            tableMappingId: '',
            columnTotalCount: 0,
            columnCheckCount: 0,
            columnNoCheckCount: 0,
            relationVals: {}
        }
        this.formRef = null

        this.debounceInitLeftList = debounce(this.debounceInitLeftList, 800)
        this.debounceInitRightTable = debounce(this.debounceInitRightTable, 800)
        this.relationInput = debounce(this.relationInput, 100)
    }
    componentDidMount = () => {
        const { taskId } = this.props.match.params
        this.setState({
            taskId
        }, () => {
            this.initAll()
            this.initStatisticsInfo()
        })
    }


    initStatisticsInfo = () => {
        const { taskId } = this.state
        api.getStatisticsInfo({
            taskId
        }).then(res => {
            const { columnNoCheckCount, columnCheckCount, columnTotalCount } = res.data
            this.setState({
                columnNoCheckCount, columnCheckCount, columnTotalCount
            })
        }).catch(err => { })
    }


    initAll = () => {
        this.initLeftList().then(res => {
            this.initRightTable()
        }).catch(err => { })
    }

    debounceInitLeftList = () => {
        this.setState({
            currentSelectIndex: 0
        })
        this.initAll()
    }

    debounceInitRightTable = () => {
        this.initRightTable()
    }

    initLeftList = (keyWord) => {
        const { taskId, selVal, tableId, tableMappingId } = this.state
        return api.getReadyTableInfo({
            taskId,
            keyWord: keyWord ? keyWord : this.state.keyWord,
            isSelect: selVal
        }).then(res => {
            const { data } = res
            this.setState({
                liList: data,
                taskId,
                tableId: tableId ? tableId : data.length > 0 ? data[0].tableId : '',
                keyWord: keyWord ? keyWord : this.state.keyWord,
                spinning: false,
                tableMappingId: tableMappingId ? tableMappingId : data.length > 0 ? data[0].tableMappingId : '',
            })
            return data
        }).catch(err => { })
    }

    initRightTable = () => {
        const { tableSelVal, tableKeyWord, taskId, tableMappingId } = this.state
        api.getTableInfo({
            taskId,
            tableMappingId,
            keyWord: tableKeyWord,
            isUsed: tableSelVal
        }).then(res => {
            const { titleHead, data } = res
            let newColumns = [...titleHead]
            let newVals = JSON.parse(JSON.stringify(this.state.relationVals))
            data.map(item => {
                newVals[item.rowKey] = item.rel ? item.rel : undefined
                item['rel'] = item.rel ? item.rel : item.relList.length > 0 ? item.relList[0].code : null
            })
            newColumns.push({
                title: '启用',
                dataIndex: 'action',
                key: 'action',
                width: '300px',
                fixed: 'right',
                render: (text, record, index) => {
                    return (
                        <Switch checked={record.isUsed === 'yes' ? true : false} onChange={this.switchChange.bind(this, record)} />
                    )
                }
            })
            newColumns.map((item, index) => {
                if (item.title === '序号') {
                    item['width'] = '100px'
                } else if (item.title === '关系') {
                    item.title = '关系'
                    item.render = (text, record, index) => {
                        return (
                            <AutoComplete
                                className={record.isUsed === 'yes' ? (!record.rel ? 'relation-empty' : '') : ''}
                                style={{ width: 200 }}
                                placeholder="选择或者输入别名"
                                defaultValue={record.rel ? record.rel : (record.relList.length > 0 ? record.relList[0].code : null)}
                                onChange={this.relationInput.bind(this, record, index)}
                            >
                                {
                                    record.relList.map(item => {
                                        return (
                                            <Option key={item.code}>{item.value}</Option>
                                        )
                                    })
                                }
                            </AutoComplete>
                        )
                    }
                } else if (item.key === 'startName') {
                    item.render = (text, record, index) => {
                        return (
                            <Tooltip title={record.seartTip}>
                                <span>{text}</span>
                            </Tooltip>
                        )
                    }
                } else if (item.key === 'endName') {
                    item.render = (text, record, index) => {
                        return (
                            <Tooltip title={record.endTip}>
                                <span>{text}</span>
                            </Tooltip>
                        )
                    }
                }
            })
            this.setState({
                columns: newColumns,
                relationVals: newVals,
                dataSource: data,
                tableLoading: false,
                spinning: false
            })
        }).catch(err => {
            this.setState({
                columns: [],
                dataSource: [],
                tableLoading: false,
                spinning: false
            })
        })
    }

    relationInput(record, index, value) {
        let newVals = JSON.parse(JSON.stringify(this.state.dataSource))
        newVals.map(item => {
            if (item.rowKey === record.rowKey) {
                item.rel = value
            }
        })
        this.setState({
            dataSource: newVals
        })
    }

    switchChange(record, value) {
        let newVals = JSON.parse(JSON.stringify(this.state.dataSource))
        newVals.map(item => {
            if (item.rowKey === record.rowKey) {
                item.isUsed = value ? 'yes' : 'no'
            }
        })
        this.setState({
            dataSource: newVals
        })
    }

    liClick(item, index) {
        this.setState({
            currentSelectIndex: index,
            tableId: item.tableId,
            tableLoading: true,
            tableMappingId: item.tableMappingId,
            dataSource: []
        }, () => {
            this.initRightTable()
        })
    }

    selChange(type, value) {
        if (type === 'leftSel') {
            this.setState({
                selVal: value,
                spinning: true,
                currentSelectIndex: 0,
                tableLoading: true,
                dataSource: [],
                tableId: '',
                tableMappingId: ''
            }, () => {
                this.initAll()
            })
        } else {
            this.setState({
                tableSelVal: value,
                tableLoading: true
            }, () => {
                this.initRightTable()
            })
        }

    }

    inputChange(type, { target: { value } }) {
        if (type === 'tableName') {
            this.setState({
                keyWord: value,
                tableId: '',
                tableMappingId: ''
            }, () => {
                this.debounceInitLeftList()
            })
        } else if (type === 'tableWord') {
            this.setState({
                tableKeyWord: value
            }, () => {
                this.debounceInitRightTable()
            })
        }
    }

    btnClick(type) {
        if (type === 'cancel') {
            window.close()
        } else if (type === 'save') {
            let params = []
            const { taskId, dataSource, relationVals, tableMappingId } = this.state
            let flag = false
            dataSource.map(item => {
                if (item.isUsed === 'yes' && (!item.rel)) {
                    flag = true
                }
                if (item.rel) {
                    params.push({
                        resultId: item.resultId,
                        taskId,
                        attrStart: item.attrStart,
                        attrEnd: item.attrEnd,
                        attrUnique: item.attrUnique,
                        rel: item.rel,
                        isUsed: item.isUsed
                    })
                }
            })
            if (flag) {
                message.error('启用的数据存在未填关系！')
            } else {
                api.saveRDF(params, tableMappingId).then(res => {
                    message.success('保存成功！')
                    this.initLeftList()
                }).catch(err => { })
            }
        } else if (type === 'submit') {
            const { taskId } = this.state
            api.confirmSubmitInfo({
                taskId
            }).then(res => {
                const { publishComfirm, publishState } = res.data
                this.setState({
                    modalContent: publishComfirm,
                    publishState,
                    modalVisible: true,
                    modalTitle: ('发布')
                })
            }).catch(err => { })
        }
    }


    confirmClick = () => {
        const { taskId } = this.state
        api.confirmPublish({
            taskId
        }).then(res => {
            message.loading('任务实例生成中...', 0)
            api.createInstance({
                taskId
            }).then(res => {
                message.destroy()
                message.loading('任务实例生成成功！')
                window.opener.location.reload();
                this.setState({
                    modalVisible: false
                }, () => {
                    setTimeout(() => {
                        window.close()
                    }, 500);
                })
            }).catch(err => { })
        }).catch(err => { })
    }


    render() {
        const {
            dataSource, columns, pageSize, pageCount, pageTotal,
            liList, currentSelectIndex, spinning, tableLoading,
            modalVisible, modalContent, publishState, modalTitle,
            columnTotalCount, columnCheckCount, columnNoCheckCount
        } = this.state
        return (
            <div className='mds-all-layout-wrap' >
                <LayoutHeader />
                <div className='mds-executePage-wrap'>
                    <div className='mds-executePage-contentWrap' style={{ display: 'flex', flex: '1', height: '100%', overflowY: 'auto' }}>
                        <Spin spinning={spinning} tip='加载中...' style={{ zIndex: '1' }}>
                            <div className='mds-executePage-leftWrap' id='tooltipWrap'>
                                <div className='mds-executePage-conditionWrapL'>
                                    <Select onSelect={this.selChange.bind(this, 'leftSel')} placeholder='请选择' style={{ width: '150px', marginRight: '5px' }}>
                                        <Option value='all'>全部</Option>
                                        <Option value='yes'>已选择</Option>
                                        <Option value='no'>未选择</Option>
                                    </Select>
                                    <Input placeholder='请输入表名' onChange={this.inputChange.bind(this, 'tableName')} />
                                </div>
                                <ul className='mds-executePage-ulWrap'>
                                    {
                                        liList.length > 0
                                            ?
                                            liList.map((item, index) => {
                                                return (
                                                    <Tooltip
                                                        key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
                                                        getPopupContainer={() => document.getElementById('tooltipWrap')}
                                                        overlayClassName='liToolTip'
                                                        placement='right'
                                                        title={
                                                            <div dangerouslySetInnerHTML={{ __html: item.levitationTips }}>
                                                            </div>
                                                        }
                                                    >
                                                        <li
                                                            onClick={this.liClick.bind(this, item, index)}
                                                            className={
                                                                currentSelectIndex === index
                                                                    ?
                                                                    'mds-executePage-liWrap mds-executePage-liCurrentSelect'
                                                                    :
                                                                    (
                                                                        item.isSelect === 'no'
                                                                            ?
                                                                            'mds-executePage-liWrap mds-executePage-liNoSelect'
                                                                            :
                                                                            'mds-executePage-liWrap mds-executePage-liSelect'
                                                                    )

                                                            }
                                                        >
                                                            <span className='mds-executePage-liName'>{item.tableName}</span>
                                                        </li>
                                                    </Tooltip>
                                                )
                                            })
                                            :
                                            <Empty style={{ marginTop: '35px' }} />
                                    }
                                </ul>
                            </div>
                        </Spin>
                        <div className='mds-executePage-RightWrap'>
                            <div className='mds-executePage-conditionWrapR'>
                                <Space size={'large'}>
                                    <Button type='primary' onClick={this.btnClick.bind(this, 'save')}>保存</Button>
                                    {/* <label>字段总数：{columnTotalCount}</label>
                                    <label>已选择字段数：{columnCheckCount}</label>
                                    <label>未选择字段数：{columnNoCheckCount}</label> */}
                                </Space>
                                <div className='mds-conditionWrap'>
                                    <SelectWrap
                                        label='状态'
                                        list={
                                            [
                                                { value: '全部', code: 'all' },
                                                { value: '已启用', code: 'yes' },
                                                { value: '未启用', code: 'no' }
                                            ]
                                        }
                                        selChange={this.selChange.bind(this, 'RightSel')}
                                    />
                                    <InputWrap label='属性名' inputChange={this.inputChange.bind(this, 'tableWord')} />
                                    <MyButton
                                        label='搜索'
                                        onClick={() => this.initRightTable()}
                                    />
                                </div>
                            </div>
                            <div style={{ padding: '10px' }}>
                                <MyTable
                                    dataSource={dataSource}
                                    columns={columns}
                                    loading={tableLoading}
                                    pageCount={pageCount}
                                    pageSize={pageSize}
                                    pageChange={this.pageChange}
                                    total={pageTotal}
                                    scroll={{ x: 900 }}
                                />
                            </div>
                        </div>
                    </div>
                    <div className='mds-executePage-btns' >
                        <MyButton
                            label='提交'
                            onClick={this.btnClick.bind(this, 'submit')}
                            style={{ margin: '0 40px' }}
                        />
                        <MyButton
                            label='返回'
                            onClick={this.btnClick.bind(this, 'cancel')}
                            classType='warm'
                        />
                    </div>
                </div>
                <MyModal
                    visible={modalVisible}
                    modalCancel={() => {
                        this.setState({
                            modalVisible: false
                        })
                    }}
                    title={modalTitle}
                    footer={
                        publishState === '01'
                            ?
                            <Button type={'primary'} onClick={() => {
                                this.setState({
                                    modalVisible: false
                                })
                            }}>
                                {'确定'}
                            </Button>
                            :
                            <Fragment>
                                <Button type={'primary'} onClick={this.confirmClick}>确定</Button>
                                <MyButton label='取消' classType='warm' onClick={() => {
                                    this.setState({
                                        modalVisible: false
                                    })
                                }} />
                            </Fragment>
                    }
                >
                    <Fragment>
                        <p dangerouslySetInnerHTML={{ __html: modalContent }}></p>
                    </Fragment>
                </MyModal>
            </div >
        )
    }
}
