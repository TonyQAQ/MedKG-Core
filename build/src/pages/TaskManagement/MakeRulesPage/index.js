import React, { Fragment, Component } from 'react'
import { Input, List, Select, Space, Tooltip, Button, Spin, message, Popover, Modal, Form, AutoComplete, Table, Empty } from 'antd';
import { SelectWrap, InputWrap, MyButton } from '@/components/commonWrap'
import LayoutHeader from '@/components/Header'
import MyTable from '@/components/Table'
import * as api from './services'
import debounce from 'lodash/debounce';
import MyModal from '@/components/Modal'
import { DownOutlined, PlusSquareTwoTone, CloseCircleOutlined } from '@ant-design/icons'
import InfiniteScroll from 'react-infinite-scroller'
import 'rc-color-picker/assets/index.css';
import '../../common.less'

const { Option } = Select

const options = [
    { value: 'Burns Bay Road' },
    { value: 'Downing Street' },
    { value: 'Wall Street' },
];

export default class Index extends Component {
    constructor(props) {
        super(props)
        this.state = {
            pageCount: 1,
            pageSize: 10,
            pageTotal: 2,
            liList: [],
            taskId: '',
            dataSource: [],
            columns: [],
            dataSource2: [],
            columns2: [],
            titleHead: [],
            currentSelectIndex: 0,
            selVal: '',
            keyWord: '',
            tableId: '',
            tableLoading: false,
            spinning: false,
            tableSelVal: '',
            tableKeyWord: '',
            editInputVal: '',
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
            hoverIndex: '',
            filterCodeTable: [],
            typeCodeTable: [],
            rulesIsSelect: {},
            filterList: [],
            inputAllValues: {},
            dataFilterings: [],
            addModalTile: '',
            addModalVisible: false,
            filteringIndex: 0,
            tableMappingId: '',
            attrMappingId: '',
            submitData: [],
            columnTotalCount: 0,
            columnCheckCount: 0,
            columnNoCheckCount: 0
        }
        this.formRef = null
        this.fieldsList = {}
        this.addList = {}
        this.debounceInitLeftList = debounce(this.debounceInitLeftList, 800)
        this.debounceInitRightTable = debounce(this.debounceInitRightTable, 800)
    }
    componentDidMount = () => {
        const { taskId } = this.props.match.params
        this.setState({
            taskId
        }, () => {
            this.initAll()
            // this.initTableRules()
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

    aliasInput(record, index, value, option) {
        let newInputAllValues = { ...this.state.inputAllValues }
        let newSubMitData = [...this.state.submitData]
        newInputAllValues[record.rowKey] = value
        newSubMitData[index].attrAlias = value
        this.setState({
            inputAllValues: newInputAllValues,
            submitData: newSubMitData
        })
    }

    addDatafiltering(record, index) {
        api.tableRules({
            attrType: record.attrType
        }).then(res => {
            const { filterCodeTable, typeCodeTable } = res.data
            this.setState({
                filterCodeTable,
                typeCodeTable
            })
        }).then(res => {
            this.setState({
                addModalTile: record.attrName,
                addModalVisible: true,
                filterList: record.filterList,
                filteringIndex: index,
                attrMappingId: record.attrMappingId,
                dataSource2: record.filterTmpList
            }, () => {
                const { filterList, typeCodeTable } = this.state
                let newColumns = [...record.titleHead]
                newColumns.push({
                    title: '选择',
                    dataIndex: 'choose',
                    key: 'choose',
                    width: '100px',
                    fixed: 'right',
                    render: (text, record, index) => {
                        return (
                            <Button type='primary' onClick={this.chooseClick.bind(this, record, index)}>选择</Button>
                        )
                    }
                })
                let valueList = {}
                let newRulesIsSelect = {}
                filterList.map((item, index) => {
                    if (!valueList[item.typeCode]) {
                        valueList[item.typeCode] = [{ filterId: item.filterId, filterValue: item.filterValue }]
                    } else {
                        valueList[item.typeCode].push({ filterId: item.filterId, filterValue: item.filterValue })
                    }
                })
                typeCodeTable.map(item => {
                    if (!(item.code in valueList)) {
                        valueList[item.code] = []
                    }
                })
                for (const key in valueList) {
                    if (valueList[key].length === 0) {
                        this.addList[key]({ filterId: undefined, filterValue: undefined })
                        if (!newRulesIsSelect[key]) {
                            newRulesIsSelect[key] = []
                            newRulesIsSelect[key][0] = false
                        }
                    } else {
                        const len = valueList[key].length
                        for (let index = 0; index < len; index++) {
                            this.addList[key](valueList[key][index])
                            if (!newRulesIsSelect[key]) {
                                newRulesIsSelect[key] = []
                                newRulesIsSelect[key][0] = true
                            } else {
                                newRulesIsSelect[key][index] = true
                            }
                        }
                    }
                }
                this.setState({
                    rulesIsSelect: newRulesIsSelect,
                    columns2: newColumns
                })
            })
        }).catch(err => { })

    }


    chooseClick(record, index) {
        const { typeCode, filterId, filterValue } = record
        this.addList[typeCode]({ filterId: filterId, filterValue: filterValue })
    }

    addModalOk = () => {
        this.formRef.submit()
    }


    addModalCancel = () => {
        this.setState({
            addModalVisible: false,
            columns2: [],
            dataSource2: []
        })
    }

    initTableRules = () => {
        api.tableRules().then(res => {
            const { filterCodeTable, typeCodeTable } = res.data
            this.setState({
                filterCodeTable,
                typeCodeTable
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
        const { taskId, selVal, tableId, tableOrigin, tableMappingId } = this.state
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
                tableOrigin: tableOrigin ? tableOrigin : data.length > 0 ? data[0].tableOrigin : '',
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
            isCheck: tableSelVal
        }).then(res => {
            const { titleHead, data } = res
            let newColumns = [...titleHead]
            let newSubMitData = JSON.parse(JSON.stringify(data))
            newSubMitData.map(item => {
                item['tableMappingId'] = tableMappingId
                item['attrAlias'] = item['attrAlias'] ? item['attrAlias'] : (item['aliasList'].length > 0 ? item['aliasList'][0].code : '')
            })
            newColumns.push({
                title: '别名',
                dataIndex: 'alias',
                key: 'alias',
                width: '300px',
                fixed: 'right',
                render: (text, record, index) => {
                    return (
                        <AutoComplete
                            style={{ width: 200 }}
                            placeholder="选择或者输入别名"
                            defaultValue={record.attrAlias ? record.attrAlias : (record.aliasList.length > 0 ? record.aliasList[0].code : null)}
                            onChange={this.aliasInput.bind(this, record, index)}
                        >
                            {
                                record.aliasList.map(item => {
                                    return (
                                        <Option key={item.code}>{item.value}</Option>
                                    )
                                })
                            }
                        </AutoComplete>
                    )
                }
            }, {
                title: "数据过滤",
                key: 'datafiltering',
                fixed: 'right',
                width: 500,
                render: (text, record, index) => {
                    return (
                        <div style={{ display: 'flex' }}>
                            <div>
                                {record.filterList.map((item, index) => {
                                    return (
                                        <span
                                            key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}>
                                            {item.filterTip + (index === record.filterList.length - 1 ? '' : '、')}
                                        </span>
                                    )
                                })}
                            </div>
                            <PlusSquareTwoTone onClick={this.addDatafiltering.bind(this, record, index)} style={{ fontSize: '20px', marginLeft: '5px' }} />
                        </div>
                    )
                }
            })
            newColumns.map((item, index) => {
                if (item.title === '序号') {
                    item['width'] = '100px'
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
                                                    renderItem={item => (
                                                        <List.Item key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}>
                                                            <div>{item}</div>
                                                        </List.Item>
                                                    )}
                                                >
                                                    {
                                                        this.state.hoverList.length === 0
                                                            ?
                                                            <Empty style={{ marginTop: '35px' }} />
                                                            :
                                                            null
                                                    }
                                                    {this.state.hoverLoading && this.state.hasMore && (
                                                        <div className="demo-loading-container">
                                                            <Spin />
                                                        </div>
                                                    )}
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
                } else if (item.title === "状态") {
                    item.render = (text, record, index) => {
                        if (record.isCheck === 'no') {
                            return (
                                <div className='mds-taskMng-rowState'>
                                    <span className='state-icon label'></span>
                                    <label>{record.attrState}</label>
                                </div>
                            )
                        } else {
                            return (
                                <div className='mds-taskMng-rowState'>
                                    <span className='state-icon rule'></span>
                                    <label>{record.attrState}</label>
                                </div>
                            )
                        }
                    }
                }
            })
            this.setState({
                submitData: newSubMitData,
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
            message.warning('已经到底了！');
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
                // columnName: '',
                hoverLoading: false,
                hasMore: true,
            })
        }
    }

    liClick(item, index) {
        this.setState({
            currentSelectIndex: index,
            tableId: item.tableId,
            tableLoading: true,
            tableOrigin: item.tableOrigin,
            inputAllValues: {},
            tableMappingId: item.tableMappingId,
            submitData: []
        }, () => {
            this.initRightTable()
        })
    }

    selChange(type, value) {
        if (type === 'leftSel') {
            this.setState({
                selVal: value,
                spinning: true,
                tableLoading: true,
                currentSelectIndex: 0,
                dataSource: [],
                tableId: '',
                tableMappingId: '',
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
                tableMappingId: '',
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

    btnClick(type) {
        if (type === 'cancel') {
            window.close()
        } else if (type === 'save') {
            const { submitData } = this.state
            let params = []
            submitData.map(item => {
                params.push({
                    tableMappingId: item.tableMappingId,
                    attrMappingId: item.attrMappingId,
                    attrAlias: item.attrAlias,
                    filterList: item.filterList.map(item2 => {
                        return {
                            filterId: item2.filterId,
                            filterValue: item2.filterValue
                        }
                    })
                })
            })
            api.saveRulesInfo(params).then(res => {
                message.success('保存成功！')
                this.initLeftList()
                this.initStatisticsInfo()
            }).catch(err => { })

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


    onFinish = (values) => {
        const { filteringIndex, filterCodeTable, submitData } = this.state
        let newSubMitData = JSON.parse(JSON.stringify(submitData))
        newSubMitData[filteringIndex].filterList = []
        for (const key in values) {
            values[key].map(item => {
                if (item && item.filterId) {
                    let content = ''
                    const len = filterCodeTable[key].length
                    for (let index = 0; index < len; index++) {
                        if (filterCodeTable[key][index].code === item.filterId) {
                            content = filterCodeTable[key][index].value + item.filterValue
                            break;
                        }
                    }
                    newSubMitData[filteringIndex].filterList.push({
                        filterId: item.filterId,
                        filterValue: item.filterValue,
                        filterTip: content,
                        typeCode: key
                    })
                }
            })
        }
        this.setState({
            dataSource: newSubMitData,
            addModalVisible: false,
            submitData: newSubMitData
        })
    }


    // 
    ruleSelChange(code, index, value) {
        let newRulesIsSelect = { ...this.state.rulesIsSelect }
        newRulesIsSelect[code][index] = (value ? true : false)
        if (!value) {
            let result = this.formRef.getFieldsValue()
            result[code][index] = { filterId: undefined, filterValue: undefined }
            this.formRef.setFieldsValue(result[code][index])
        }
        this.setState({
            rulesIsSelect: newRulesIsSelect
        })
    }

    render() {
        const {
            dataSource, columns, pageSize, pageCount, pageTotal,
            liList, currentSelectIndex, spinning, tableLoading,
            modalVisible, modalContent, publishState, modalTitle, addModalTile,
            addModalVisible, typeCodeTable, columns2, dataSource2,
            filterCodeTable, rulesIsSelect, columnTotalCount, columnCheckCount,
            columnNoCheckCount
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
                                        label='字段选择'
                                        list={
                                            [
                                                { value: '全部', code: 'all' },
                                                { value: '已校验', code: 'yes' },
                                                { value: '未校验', code: 'no' }
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
                <Modal
                    title={<div style={{ textAlign: 'center' }}>{addModalTile}</div>}
                    visible={addModalVisible}
                    onCancel={this.addModalCancel}
                    destroyOnClose
                    maskClosable={false}
                    forceRender
                    width="50%"
                    style={{
                        top: '10px'
                    }}
                    bodyStyle={{
                        maxHeight: '82vh',
                        overflowY: 'auto'
                    }}
                    footer={(
                        <Fragment>
                            <Button type={'primary'} onClick={this.addModalOk}>确定</Button>
                            <MyButton label='取消' classType='warm' onClick={this.addModalCancel} />
                        </Fragment>
                    )}
                >
                    <div className='mds-datafiltering-itemWrap'>
                        <div className='mds-datafiltering-modalTableWrap' >
                            <Table
                                dataSource={dataSource2}
                                columns={columns2}
                                pagination={false}
                            />
                        </div>
                        <Form
                            name="dynamic_form_item"
                            onFinish={this.onFinish}
                            ref={(ref) => this.formRef = ref}
                            style={{
                                width: '50%'
                            }}
                        >
                            {
                                typeCodeTable.map((item, index1) => {
                                    return (
                                        <Form.List name={item.code} key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}>
                                            {(fields, { add, remove }) => {
                                                this.fieldsList[item.code] = fields
                                                if (this.fieldsList[item.code].length === 0) {
                                                    this.addList[item.code] = add
                                                }
                                                return (
                                                    <div style={{
                                                        display: 'flex',
                                                        flexWrap: 'wrap',
                                                        position: 'relative',
                                                        margin: '10px 0 5px 0 ',
                                                        borderBottom: index1 === typeCodeTable.length - 1 ? null : '1px solid #ddd'
                                                    }}>
                                                        {this.fieldsList[item.code].map((field, index2) => (
                                                            <Fragment>
                                                                {
                                                                    index2 === 0
                                                                        ?
                                                                        <div key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)} className='mds-fields-label'>
                                                                            {item.value}
                                                                        </div>
                                                                        :
                                                                        null
                                                                }
                                                                <div>
                                                                    <div key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)} >
                                                                        <Space
                                                                            key={field.key}
                                                                            style={{ display: 'flex', marginBottom: 8 }}
                                                                            align="start">
                                                                            <Form.Item
                                                                                {...field}
                                                                                name={[field.name, 'filterId']}
                                                                                fieldKey={[field.fieldKey, 'filterId']}
                                                                                style={{ display: 'inline-block', width: '110px', marginRight: '5px' }}
                                                                                rules={[{ required: rulesIsSelect[item.code] ? rulesIsSelect[item.code][index2] : false, message: '未选择！' }]}
                                                                            >
                                                                                <Select allowClear
                                                                                    onChange={this.ruleSelChange.bind(this, item.code, index2)}
                                                                                    placeholder="请选择">
                                                                                    {
                                                                                        filterCodeTable[item.code].map((selItem, selIndex) => {
                                                                                            return (
                                                                                                <Option key={selItem.code}>{selItem.value}</Option>
                                                                                            )
                                                                                        })
                                                                                    }
                                                                                </Select>
                                                                            </Form.Item>
                                                                            <Form.Item
                                                                                {...field}
                                                                                name={[field.name, 'filterValue']}
                                                                                fieldKey={[field.fieldKey, 'filterValue']}
                                                                                style={{ display: 'inline-block', width: '210px' }}
                                                                                rules={[{ required: rulesIsSelect[item.code] ? rulesIsSelect[item.code][index2] : false, message: '未输入！' }]}
                                                                            >
                                                                                <Input placeholder="请输入" />
                                                                            </Form.Item>
                                                                            {
                                                                                index2 === 0
                                                                                    ?
                                                                                    <PlusSquareTwoTone
                                                                                        style={{
                                                                                            fontSize: "20px",
                                                                                            color: 'red',
                                                                                            margin: '5px'
                                                                                        }}
                                                                                        onClick={() => {
                                                                                            add(1);
                                                                                        }} />
                                                                                    :
                                                                                    null
                                                                            }
                                                                            {
                                                                                index2 > 0
                                                                                    ?
                                                                                    <CloseCircleOutlined
                                                                                        style={{
                                                                                            fontSize: "20px",
                                                                                            color: 'red',
                                                                                            margin: '5px'
                                                                                        }}
                                                                                        onClick={() => {
                                                                                            remove(field.name);
                                                                                        }}
                                                                                    />
                                                                                    :
                                                                                    null
                                                                            }
                                                                        </Space>
                                                                    </div>
                                                                </div>
                                                            </Fragment>
                                                        ))}
                                                    </div>
                                                );
                                            }}
                                        </Form.List>
                                    )
                                })
                            }
                        </Form>
                    </div>
                </Modal>
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
