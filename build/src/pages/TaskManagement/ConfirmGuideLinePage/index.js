import React, { useState, useEffect, Fragment } from 'react'
import LayoutHeader from '@/components/Header'
import { message, Empty, Spin, Input, Select, Modal, Tabs, Pagination, Tooltip, Button, InputNumber, Space } from 'antd'
import { MyButton } from '@/components/commonWrap';
import { Annotator } from 'poplar-annotation';
import { InfoCircleTwoTone } from '@ant-design/icons';
import * as api from './services'
import $ from 'jquery';
import './index.less'

const { Option } = Select
const { TabPane } = Tabs;

export default function Index(props) {

    const [liList, setLiList] = useState([])
    const [taskId, setTaskId] = useState(null)
    const [labelTypeList, setLabelTypeList] = useState([])
    const [keyWord, setKeyWord] = useState(null)
    const [typeCode, setTypeCode] = useState(null)
    const [isSelect, setIsSelect] = useState(null)
    const [currentSelectIndex, setCurrentSelectIndex] = useState(0)
    const [spinning, setSpinning] = useState(true)
    const [pageCurrent, setPageCurrent] = useState(1)
    const [pageSize, setPageSize] = useState(5)
    const [pageTotal, setPageTotal] = useState(0)
    const [tabldLoading, setTabldLoading] = useState(true)
    const [schemaList, setSchemaList] = useState([])
    const [tag, setTag] = useState('01')
    const [schemaId, setSchemaId] = useState(null)
    const [definition, setDefinition] = useState('')
    const [tabPaneKey, setTabPaneKey] = useState('1')
    const [modalVisible, setModalVisible] = useState(false)
    const [maxCount, setMaxCount] = useState(0)
    const [trainCount, setTrainCount] = useState(0)

    useEffect(() => {
        const { taskId } = props.match.params
        setTaskId(taskId)

    }, [])

    useEffect(() => {
        if (taskId) {
            getLabelTypeList()
            api.getMaxCount({ taskId }).then(res => {
                setMaxCount(res.maxTrainCount)
            })
        }
    }, [taskId])

    // 获取左侧列表
    useEffect(() => {
        if (taskId && typeCode) {
            getLiList()
        }
    }, [taskId, typeCode])

    //获取右侧列表
    useEffect(() => {
        if (currentSelectIndex !== '' && schemaId && pageCurrent && tag) {
            getSchemaJsonList()
        }
    }, [currentSelectIndex, schemaId, pageCurrent, tag])


    // 获取标签分类
    const getLabelTypeList = () => {
        api.getLabelTypeList({
            taskId
        }).then(res => {
            setLabelTypeList(res.data)
            setTypeCode(res.data.length > 0 ? res.data[0].code : null)
        }).catch(err => { })
    }

    // 获取左侧列表
    const getLiList = (type) => {
        api.getLiList({
            taskId,
            typeCode,
            isSelect,
            keyWord
        }).then(res => {
            setLiList(res.data)
            if (!type) {
                setSchemaId(res.data.length > 0 ? res.data[0].schemaId : null)
                setDefinition(res.data.length > 0 ? res.data[0].definition : null)
            }
            setSpinning(false)
        }).catch(err => { })
    }

    // 获取右侧列表
    const getSchemaJsonList = () => {
        api.getSchemaJsonList({
            schemaId,
            tag,
            pageSize,
            pageCount: pageCurrent
        }).then(res => {
            setSchemaList(res.data)
            setTabldLoading(false)
            setPageTotal(res.totalCount)
        }).catch(err => { })
    }

    // 标签类别选择
    const selChange = (value) => {
        setSpinning(true)
        setTypeCode(value)
    }


    // tabs切换事件
    const tabChange = (value) => {
        if (tabPaneKey !== value) {
            $('.svgContainer').remove()
            setTabldLoading(true)
            setSchemaList([])
            setPageCurrent(1)
            setTabPaneKey(value)
            setTag(value === '1' ? '01' : '02')
        }
    }

    // 翻页
    const pageChange = (page, pageSize) => {
        $('.svgContainer').remove()
        setTabldLoading(true)
        setSchemaList([])
        setPageCurrent(page)
    }

    // 渲染SVG
    const renderSvg = (item, index) => {
        const wrap = tag === '01' ? "#svgAllContainer1" : "#svgAllContainer2"
        const len = $(wrap).children("#svgContainer" + index).length
        if (len === 0) {
            $(wrap).append("<div class='svgContainer' id='svgContainer" + index + "'></div>")
        }
        let svgDom = document.getElementById('svgContainer' + index)
        // const tempArry = JSON.parse(JSON.stringify(this.state.labelCategories))
        // const tempArry2 = JSON.parse(JSON.stringify(this.state.connectionCategories))
        if (svgDom && $('#svgContainer' + index).children().length === 0) {
            let annotator = new Annotator(item.annotation, document.getElementById('svgContainer' + index), {
                labelWidthCalcMethod: 'max',
                unconnectedLineStyle: 'none'
            })
        }
    }

    // 输入标签名
    const inputChange = ({ target: { value } }) => {
        setKeyWord(value)
    }

    const modalCancel = () => {
        setModalVisible(false)
    }

    const modalOk = () => {
        if (trainCount) {
            api.finish({
                taskId,
                trainCount
            }).then(res => {
                message.success('操作成功！')
                window.opener.location.reload();
                setTimeout(() => {
                    window.close()
                }, 500);
            }).catch(err => { })
        } else {
            message.error('不能为空！')
        }
    }

    const numberChange = (value) => {
        setTrainCount(value)
    }

    function liClick(item, index) {
        if (currentSelectIndex !== index) {
            const { definition, schemaId } = item
            $('.svgContainer').remove();
            setCurrentSelectIndex(index)
            setSchemaId(schemaId)
            setTag('01')
            setTabPaneKey('1')
            setTabldLoading(true)
            setSchemaList([])
            setDefinition(definition)
        }
    }

    function btnClick(type) {
        if (type === 'preve') {
            window.opener.location.reload();
            window.location.href = '#/SchemaCheckOut/taskId=' + taskId
        } else if (type === 'cancel') {
            window.close()
        } else if (type === 'finish') {
            setModalVisible(true)
        }
    }

    return (
        <div className='mds-all-layout-wrap' >
            <LayoutHeader />
            <div className='mds-executePage-wrap'>
                <div className='mds-executePage-contentWrap' style={{ display: 'flex', flex: '1', height: '100%', overflowY: 'auto' }}>
                    <Spin spinning={spinning} tip='加载中...' style={{ zIndex: '1' }}>
                        <div className='mds-executePage-leftWrap' id='tooltipWrap'>
                            <div className='mds-executePage-conditionWrapL'>
                                <Select value={typeCode} onChange={selChange} placeholder='请选择' style={{ marginRight: '5px' }}>
                                    {
                                        labelTypeList.map(item => {
                                            return (
                                                <Option value={item.code}>{item.value}</Option>
                                            )
                                        })
                                    }
                                </Select>
                                <Input onChange={inputChange} onPressEnter={() => {
                                    setCurrentSelectIndex(null)
                                    getLiList('handle')
                                }} placeholder='请输入标签名' />
                            </div>
                            <ul className='mds-executePage-ulWrap'>
                                {
                                    liList.length > 0
                                        ?
                                        liList.map((item, index) => {
                                            return (
                                                <li
                                                    key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
                                                    onClick={() => liClick(item, index)}
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
                                                    <span className='mds-executePage-liName'>{item.schemaTip}</span>
                                                </li>
                                            )
                                        })
                                        :
                                        <Empty style={{ marginTop: '35px' }} />
                                }
                            </ul>
                        </div>
                    </Spin>
                    <div className='mds-executePage-middleWrap' style={{ padding: '20px' }}>
                        <div className='mds-guideLine-head'>
                            <p>标签定义</p>
                            <div className='mds-guideLine-headContent'>
                                <Tooltip title={definition}>
                                    {definition}
                                </Tooltip>

                            </div>
                            <div className='mds-guideLine-tabWrap'>
                                <Tabs activeKey={tabPaneKey} onChange={tabChange}>
                                    <TabPane tab="正面例子" key="1">
                                        <div id='svgAllContainer1'>
                                            {
                                                schemaList.length > 0
                                                    ?
                                                    schemaList.map((item, index) => {
                                                        return (
                                                            renderSvg(item, index)
                                                        )
                                                    })
                                                    :
                                                    <Empty />
                                            }
                                        </div>
                                        <Pagination
                                            className='mds-guideLine-svgPageNation'
                                            current={pageCurrent}
                                            showQuickJumper
                                            pageSize={pageSize}
                                            onChange={pageChange}
                                            total={pageTotal}
                                        />
                                    </TabPane>
                                    <TabPane tab="负面例子" key="2">
                                        <div id='svgAllContainer2'>
                                            {
                                                schemaList.length > 0
                                                    ?
                                                    schemaList.map((item, index) => {
                                                        return (
                                                            renderSvg(item, index)
                                                        )
                                                    })
                                                    :
                                                    <Empty />
                                            }
                                        </div>
                                        <Pagination
                                            className='mds-executePage-svgPageNation'
                                            current={pageCurrent}
                                            showQuickJumper
                                            pageSize={pageSize}
                                            onChange={pageChange}
                                            total={pageTotal}

                                        />
                                    </TabPane>
                                </Tabs>
                            </div>
                        </div>
                    </div>
                </div>
                <div className='mds-guideLine-btns' >
                    <MyButton
                        label='上一步'
                        onClick={() => btnClick('preve')}
                        style={{ marginRight: '40px' }}
                    />
                    <MyButton
                        label='完成'
                        onClick={() => btnClick('finish')}
                        style={{ marginRight: '40px' }}
                    />
                    {/* <MyButton
                        label='导出'
                        onClick={() => btnClick('export')}
                        style={{ marginRight: '40px' }}
                    /> */}
                    <MyButton
                        label='取消'
                        onClick={() => btnClick('cancel')}
                        classType='warm'
                    />
                </div>
                <Modal
                    title="提示"
                    visible={modalVisible}
                    onCancel={modalCancel}
                    destroyOnClose
                    footer={
                        <Fragment>
                            <Button type={'primary'} onClick={modalOk}>确定</Button>
                            <MyButton label='取消' classType='warm' onClick={modalCancel} />
                        </Fragment>
                    }
                >
                    <div>
                        <Space align='center'>
                            <InfoCircleTwoTone style={{ fontSize: '20px' }} />
                            <InputNumber min={1} max={maxCount} onChange={numberChange} />
                            <label>最大训练量：{maxCount}</label>
                        </Space>
                    </div>
                    <div>
                        <Space align='center' style={{ marginTop: '20px' }}>
                            <InfoCircleTwoTone style={{ fontSize: '20px' }} />
                            <label>提交后不可撤回，确认提交吗？</label>
                        </Space>
                    </div>

                </Modal>
            </div>
        </div>
    )
}
