import React, { Fragment, Component } from 'react'
import { Input, List, Select, Space, Tag, Tooltip, Button, Spin, message, Popover, Empty } from 'antd';
import { SelectWrap, InputWrap, MyButton } from '@/components/commonWrap'
import LayoutHeader from '@/components/Header'
import MyTable from '@/components/Table'
import * as api from './services'
import './index.less'
import debounce from 'lodash/debounce';
import MyModal from '@/components/Modal'
import { FormOutlined, DownOutlined } from '@ant-design/icons'
import InfiniteScroll from 'react-infinite-scroller'

const { Option } = Select

const { CheckableTag } = Tag;


export default class index extends Component {
    constructor(props) {
        super(props)
        this.state = {
            pageCount: 1,
            pageSize: 10,
            pageTotal: 2,
            checkList: {},
            liList: [],
            editId: '',
            taskId: '',
            dataSource: [],
            columns: [],
            titleHead: [],
            currentSelectIndex: 0,
            selVal: '',
            keyWord: '',
            tableId: '',
            tableLoading: true,
            spinning: true,
            tableSelVal: '',
            tableKeyWord: '',
            editInputVal: '',
            editIndex: '',
            columnNoSelectCount: 0,
            columnSelectCount: 0,
            columnTotalCount: 0,
            tableAlias: '',
            modalVisible: false,
            modalContent: '',
            publishState: '',
            modalTitle: '',
            hoverLoading: false,
            hasMore: true,
            hoverList: [],
            tableOrigin: '',
            hoverPage: 1,
            hoverPageSize: 10,
            hoverTotal: 0,
            hoverIndex: ''
        }
        // this.count = 0;
        this.inputRef = null
        this.debounceInitLeftList = debounce(this.debounceInitLeftList, 800)
        this.debounceInitRightTable = debounce(this.debounceInitRightTable, 800)
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
            const { columnNoSelectCount, columnSelectCount, columnTotalCount } = res.data
            this.setState({
                columnNoSelectCount, columnSelectCount, columnTotalCount
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
        const { taskId, selVal, tableId, tableOrigin } = this.state
        return api.getReadyTableInfo({
            taskId,
            keyWord: keyWord ? keyWord : this.state.keyWord,
            isSelect: selVal
        }).then(res => {
            const { data } = res
            this.setState({
                liList: data,
                taskId,
                tableId: tableId ? tableId : (data.length > 0 ? data[0].tableId : ''),
                keyWord: keyWord ? keyWord : this.state.keyWord,
                spinning: false,
                tableOrigin: tableOrigin ? tableOrigin : (data.length > 0 ? data[0].tableOrigin : '')
            })
            return data
        }).catch(err => { })
    }

    initRightTable = () => {
        const { tableSelVal, tableKeyWord, taskId, tableId } = this.state
        api.getTableInfo({
            taskId,
            tableId,
            keyWord: tableKeyWord,
            isSelect: tableSelVal
        }).then(res => {
            const { titleHead, data } = res
            let { checkList } = this.state
            data.map(item => {
                checkList[item.attrId] = (item.isSelect === 'no' ? false : true)
            })
            let newColumns = [...titleHead]
            newColumns.push({
                title: "字段选择",
                key: 'action',
                fixed: 'right',
                width: 200,
                render: (text, record) => {
                    return (
                        <Fragment>
                            <CheckableTag
                                className='checkIcon'
                                key={record.key}
                                checked={this.state.checkList[record.attrId]}
                                onChange={checked => this.handleChange(record, checked)}
                            >
                            </CheckableTag>
                        </Fragment>
                    )
                }
            })
            newColumns.map((item, index) => {
                if (item.title === '序号') {
                    item['width'] = '200px'
                } else if (item.title === '字段名') {
                    item['width'] = '350px'
                    item.render = (text, record, index2) => {
                        return (
                            <div style={{ position: 'relative' }} id={'hover-wrap' + index2}>
                                <span>{text}</span>
                                <Popover
                                    content={
                                        <div className="infinite-container">
                                            <InfiniteScroll
                                                initialLoad={false}
                                                pageStart={1}
                                                loadMore={this.handleInfiniteOnLoad}
                                                hasMore={!this.state.hoverLoading && this.state.hasMore}
                                                useWindow={false}
                                            >
                                                <List
                                                    dataSource={this.state.hoverList}
                                                    renderItem={item => {
                                                        return (
                                                            <List.Item key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}>
                                                                <div>{item}</div>
                                                            </List.Item>
                                                        )
                                                    }}
                                                >
                                                    {
                                                        this.state.hoverList.length === 0
                                                            ?
                                                            <Empty style={{ marginTop: '35px' }} />
                                                            :
                                                            null
                                                    }
                                                    {
                                                        this.state.hoverLoading && this.state.hasMore && (
                                                            <div className="demo-loading-container" style={{ textAlign: 'center' }}>
                                                                <Spin />
                                                            </div>
                                                        )
                                                    }
                                                    {
                                                        !this.state.hasMore
                                                            ?
                                                            <div style={{ textAlign: 'center', color: "#a8a8a8" }}>已经到底了</div>
                                                            :
                                                            null
                                                    }
                                                </List>
                                            </InfiniteScroll>
                                        </div>
                                    }
                                    title={null}
                                    arrowPointAtCenter={true}
                                    trigger="click"
                                    overlayStyle={{ left: 50 }}
                                    visible={index2 === this.state.hoverIndex ? true : false}
                                    onVisibleChange={this.onVisibleChange.bind(this, record, index2)}
                                    getTooltipContainer={() => document.getElementById('hover-wrap' + index2)}
                                >
                                    <DownOutlined style={{ color: "#5A8BFF", marginLeft: '30px' }} />
                                </Popover>
                            </div>
                        )
                    }
                }
            })
            this.setState({
                checkList,
                columns: newColumns,
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


    handleInfiniteOnLoad = () => {
        const { hoverList, hoverPage, hoverIndex, hoverTotal } = this.state
        if (hoverList.length >= hoverTotal) {
            this.setState({
                hoverLoading: false,
                hasMore: false
            })
            return;
        } else {
            if (hoverIndex !== '') {
                this.setState({
                    hoverLoading: true,
                    hoverPage: hoverPage + 1,
                }, () => {
                    this.initHoverList()
                })
            }
        }
    }

    pageChange = () => {

    }

    initHoverList = () => {
        const { tableOrigin, taskId, hoverPage, hoverPageSize, columnName } = this.state
        api.getHoverList({
            tableName: tableOrigin,
            taskId,
            columnName,
            pageCount: hoverPage,
            pageSize: hoverPageSize
        }).then(res => {
            this.setState({
                hoverList: this.state.hoverList.concat(res.data),
                hoverTotal: Number(res.totalCount),
                hoverLoading: false
            }, () => {
            })
        }).catch(err => {

        })
    }

    onVisibleChange(record, index2, visible) {
        if (visible) {
            const { attrName } = record
            this.setState({
                columnName: attrName,
                hoverIndex: index2
            }, () => {
                this.initHoverList()
            })
        } else if (!visible) {
            this.setState({
                hoverPage: 1,
                hoverPageSize: 10,
                hoverList: [],
                hoverIndex: '',
                hoverLoading: false,
                hasMore: true,
            })
        }
    }


    handleChange(item, checked) {
        const { taskId, tableId, tableAlias } = this.state
        const { attrId } = item
        api.selectTableFields({
            taskId,
            tableId,
            tableAlias,
            attrId,
            isSelect: checked ? 'yes' : 'no',
            type: '01'
        }).then(res => {
            this.initStatisticsInfo()
            let newList = { ...this.state.checkList }
            if (checked) {
                newList[item.attrId] = true
            } else {
                newList[item.attrId] = false
            }
            this.setState({
                checkList: newList
            })
        }).catch(err => { })
    }

    liClick(item, index) {
        this.setState({
            currentSelectIndex: index,
            tableId: item.tableId,
            tableLoading: true,
            tableOrigin: item.tableOrigin,
            tableAlias: ''
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
                tableOrigin: ''
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
                tableOrigin: ''
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

    onContextMenu(item, index, e) {
        e.preventDefault()
        if (this.state.currentSelectIndex === index) {
            this.setState({
                editId: item.tableId,
                editIndex: index
            }, () => {
                this.inputRef.focus()
            })
        } else {
            message.warning('只能修改当前选中的表哦！')
        }
    }

    inputOnBlur = () => {
        const { value } = this.inputRef.state
        if (value) {
            const { taskId, editId } = this.state
            api.changeTableName({
                taskId,
                tableId: editId,
                tableAlias: value
            }).then(res => {
                let { editIndex, liList } = this.state
                let newLiList = [...liList]
                newLiList[editIndex]['tableName'] = value
                this.inputRef = null
                this.setState({
                    editId: '',
                    editIndex: '',
                    liList: newLiList,
                    tableAlias: value
                })
            }).catch(err => {
                this.inputRef = null
                this.setState({
                    editId: '',
                    editIndex: ''
                })
            })
        } else if (value === '' || value === undefined) {
            message.destroy()
            message.error('不能为空！')
        } else {
            this.inputRef = null
            this.setState({
                editId: '',
                editIndex: ''
            })
        }
    }

    btnClick(type) {
        if (type === 'cancel') {
            window.close()
        } else if (type === 'save') {
            this.initLeftList()
            message.success('保存成功！')
        } else if (type === 'publish') {
            const { taskId } = this.state
            api.publishConfirmInfo({
                taskId
            }).then(res => {
                const { publishComfirm, publishState } = res.data
                this.setState({
                    modalContent: publishComfirm,
                    publishState,
                    modalVisible: true,
                    modalTitle: ('发布')
                })
            })
        }
    }


    confirmClick = () => {
        const { taskId } = this.state
        api.confirmPublish({
            taskId
        }).then(res => {
            message.success(res.retmsg)
            window.opener.location.reload();
            this.setState({
                modalVisible: false
            }, () => {
                setTimeout(() => {
                    window.close()
                }, 500);
            })
        }).catch(err => { })
    }

    render() {
        const {
            dataSource, columns, pageSize, pageCount, pageTotal,
            liList, currentSelectIndex, spinning, tableLoading,
            editId, columnNoSelectCount, columnSelectCount, columnTotalCount,
            modalVisible, modalContent, publishState, modalTitle
        } = this.state
        return (
            <div className='mds-all-layout-wrap'>
                <LayoutHeader />
                <div className='mds-executePage-wrap'>
                    <div className='mds-executePage-contentWrap' style={{ display: 'flex', flex: '1', height: '100%', overflowY: 'auto' }}>
                        <Spin spinning={spinning} tip='加载中...' style={{ zIndex: '1' }}>
                            <div className='mds-executePage-leftWrap' id='tooltipWrap'>
                                {
                                    editId !== ''
                                        ?
                                        <div className='edit-mask'></div>
                                        :
                                        null
                                }
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
                                                        getPopupContainer={() => document.getElementById('tooltipWrap')}
                                                        overlayClassName='liToolTip'
                                                        placement='right'
                                                        title={
                                                            <div dangerouslySetInnerHTML={{ __html: item.levitationTips }}>
                                                            </div>
                                                        }
                                                    >
                                                        {
                                                            editId === item.tableId
                                                                ?
                                                                <Input
                                                                    defaultValue={item.tableName}
                                                                    className='editInput'
                                                                    ref={ref => {
                                                                        this.inputRef = ref
                                                                    }}
                                                                    onBlur={this.inputOnBlur}
                                                                    onChange={this.inputChange.bind(this, 'edit')}
                                                                />
                                                                :
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
                                                                    {
                                                                        currentSelectIndex === index
                                                                            ?
                                                                            <FormOutlined
                                                                                className='editIcon'
                                                                                onClick={this.onContextMenu.bind(this, item, index)}
                                                                            />
                                                                            :
                                                                            null
                                                                    }
                                                                </li>
                                                        }

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
                                    <label>字段总数：{columnTotalCount}</label>
                                    <label>已选择字段数：{columnSelectCount}</label>
                                    <label>未选择字段数：{columnNoSelectCount}</label>
                                </Space>
                                <div className='mds-conditionWrap'>
                                    <SelectWrap
                                        label='字段选择'
                                        list={
                                            [
                                                { value: '全部', code: 'all' },
                                                { value: '已选择', code: 'yes' },
                                                { value: '未选择', code: 'no' }
                                            ]
                                        }
                                        selChange={this.selChange.bind(this, 'RightSel')}
                                    />
                                    <InputWrap label='字段名' inputChange={this.inputChange.bind(this, 'tableWord')} />
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
                                    scroll={{ x: 900, y: 600 }}
                                />
                            </div>
                        </div>
                    </div>
                    <div className='mds-executePage-btns' >
                        <MyButton
                            label='保存'
                            onClick={this.btnClick.bind(this, 'save')}
                        />
                        <MyButton
                            label='发布'
                            onClick={this.btnClick.bind(this, 'publish')}
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
                    // modalOk={modalOk}
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
            </div>
        )
    }
}
