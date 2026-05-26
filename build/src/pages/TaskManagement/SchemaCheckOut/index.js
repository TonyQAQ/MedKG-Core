import React, { Fragment, useEffect, useState } from 'react'
import { message, Empty, Spin, Input, Select, Button, Space, Modal, Table, Tag } from 'antd'
import LayoutHeader from '@/components/Header'
import ColorPicker from 'rc-color-picker';
import { MyButton, SelectWrap } from '@/components/commonWrap';
import { FormOutlined, MinusCircleOutlined } from '@ant-design/icons';
import ReactQuill from 'react-quill';
import { Annotator, Action } from 'poplar-annotation';
import MyModal from '@/components/Modal'
import $ from 'jquery';
import 'react-quill/dist/quill.bubble.css';
import * as api from './services'
import 'rc-color-picker/assets/index.css';
import '../../common.less'
import './index.less'

const { TextArea } = Input;

const { Option } = Select
const { CheckableTag } = Tag;

export default function Index(props) {
    const [spinning, setSpinning] = useState(true)
    const [currentSelectIndex, setCurrentSelectIndex] = useState(0)
    const [liList, setLiList] = useState([])
    const [color, setColor] = useState('')
    const [color1, setColor1] = useState('#5A8BFF')
    const [modalVisible, setModalVisible] = useState(false)
    const [labelVisible, setLabelVisible] = useState(false)
    const [modalTitle, setModalTitle] = useState('')
    const [pageCurrent, setPageCurrent] = useState(1)
    const [pageSize, setPageSize] = useState(5)
    const [pageTotal, setPageTotal] = useState(0)
    const [taskId, setTaskId] = useState('')
    const [labelTypeList, setLabelTypeList] = useState([])
    const [typeCode, setTypeCode] = useState(null)
    const [typeCode1, setTypeCode1] = useState(null)
    const [isSelect, setIsSelect] = useState('all')
    const [editorHtml, setEditorHtml] = useState('')
    const [schemaId, setSchemaId] = useState(null)
    const [userId, setUserId] = useState('all')
    const [tag, setTag] = useState('all')
    const [schemaList, setSchemaList] = useState([])
    const [tabldLoading, setTabldLoading] = useState(true)
    const [checkList, setCheckList] = useState(null)
    const [columns, setColumns] = useState([])
    const [stateCode, setStateCode] = useState('')
    const [schemaName, setSchemaName] = useState('')
    const [borderColor, setBorderColor] = useState('')
    const [labelName, setLabelName] = useState('')
    const [definition, setDefinition] = useState('')
    const [focusIndex, setFocusIndex] = useState(null)
    const [eventType, setEventType] = useState('')
    const [userList, setUserList] = useState([])
    const [guideLineVisible, setGuideLineVisible] = useState(false)
    const [modalContent, setModalContent] = useState('')
    const [publishState, setPublishState] = useState('')
    const [itemObj, setItemObj] = useState({})
    const [iconType, setIconType] = useState('')

    useEffect(() => {
        const doms = $('.svgContainer').length
        if (doms) {
            schemaList.map((item, index) => {
                renderSvg(item, index)
            })
        }
    })

    useEffect(() => {
        const { taskId } = props.match.params
        setTaskId(taskId)
    }, [])

    useEffect(() => {
        if (taskId) {
            getLabelTypeList()
            getUsers()
        }
    }, [taskId])

    useEffect(() => {
        if (taskId && (typeCode || isSelect)) {
            getSchemaList()
        }
    }, [typeCode, isSelect])

    useEffect(() => {
        if (currentSelectIndex !== '' && currentSelectIndex !== null && schemaId && pageCurrent && userId && tag && isSelect) {
            getSchemaJsonList()
        }
    }, [currentSelectIndex, schemaId, pageCurrent, userId, tag, isSelect])

    // useEffect(() => {
    //     if (pageCurrent) {
    //         getSchemaJsonList()
    //     }
    // }, [pageCurrent])

    const getUsers = () => {
        api.getUsers({ taskId }).then(res => {
            setUserList(res.data)
        })
    }

    const getSchemaList = (type) => {
        api.getSchemaList({
            taskId,
            typeCode,
            isSelect
        }).then(res => {
            setStateCode(res.litleStateCode)
            setLiList(res.data)
            if (!type && res.data.length > 0) {
                setSchemaId(res.data[0].schemaId)
                setColor(res.data[0].color)
                setLabelName(res.data[0].labelName)
                setBorderColor(res.data[0].borderColor)
                setSchemaName(res.data[0].schemaName)
                setDefinition(res.data[0].definition)
                setTypeCode1(res.data[0].typeCode)
            }
            setSpinning(false)
        })
    }

    const getSchemaJsonList = () => {
        setTabldLoading(true)
        api.getSchemaJsonList({
            schemaId,
            attendUserId: userId,
            tag,
            pageSize,
            pageCount: pageCurrent
        }).then(res => {
            let newCheckList = {}
            res.data.map(item => {
                newCheckList[item.sampleId] = item.tag
            })
            setCheckList(newCheckList)
            setSchemaList(res.data)
            setTabldLoading(false)
            setPageTotal(res.totalCount)
        }).catch(err => { })
    }

    const getLabelTypeList = () => {
        api.getLabelTypeList().then(res => {
            setLabelTypeList(res.data)
            setTypeCode1(res.data[0].code)
            setTypeCode(res.data[0].code)
        }).catch(err => { })
    }

    const textAreaChange = ({ target: { value } }) => {
        setDefinition(value)
    }

    const colorChange = (value, type) => {
        if (type === '1') {
            setColor1(value.color)
        } else {
            setColor1(value.color)
            setColor(value.color)
        }
    }

    const modalCancel = () => {
        setModalVisible(false)
        setLabelVisible(false)
        setColor(itemObj.color)
        setSchemaName('')
        setEditorHtml('')
    }

    const editorChange = (value) => {
        setEditorHtml(value)
    }

    const labelInputChange = ({ target: { value } }) => {
        setSchemaName(value)
    }

    const pageChange = (page, pageSize) => {
        setPageCurrent(page)
        $('.svgContainer').remove();
        setSchemaList([])
        setCheckList({})
        setTabldLoading(true)
    }

    function modalOk(type) {
        if (type === 'new' || type === 'edit' || type === 'save') {
            if ((type === 'new' || type === 'edit') && schemaName === '') {
                message.error('输入不能为空！')
            } else {
                if (currentSelectIndex === null) {
                    message.error('请先选择标签！')
                } else {
                    api.addOrUpdateSchema({
                        schemaId: type === 'new' ? '' : schemaId,
                        schemaName,
                        definition: type === 'new' ? '' : definition,
                        typeCode: typeCode1,
                        labelName: type === 'new' ? '' : labelName,
                        color: type === 'save' ? color : color1,
                        borderColor: type === 'new' ? '' : borderColor,
                        taskId
                    }).then(res => {
                        message.success('操作成功！')
                        setLabelVisible(false)

                        if (type === 'new') {
                            setLiList([])
                            getSchemaList('handle')
                        } else {
                            setColor1('#5A8BFF')
                            $('.svgContainer').remove();
                            setSchemaList([])
                            setCheckList({})
                            setColor(color1)
                            setLiList([])
                            setSpinning(true)
                            setTabldLoading(true)
                            getSchemaList('handle')
                            getSchemaJsonList()
                        }
                    }).catch(err => { })
                }
            }
        } else if (type === 'guideLine') {
            window.opener.location.reload();
            window.location.href = '#/ConfirmGuideLinePage/taskId=' + taskId
        }
    }

    function selChange(value, type) {
        if (type === 'typeCode') {
            $('.svgContainer').remove();
            setTypeCode(value)
            setUserId('all')
            setTag('all')
            setCurrentSelectIndex(0)
            setPageCurrent(1)
            setSchemaList([])
            setCheckList({})
            setSpinning(true)
            setSchemaId(null)
        } else if (type === 'typeSel1') {
            setTypeCode1(value)
        } else if (type === 'user') {
            setPageCurrent(1)
            setUserId(value)
        } else if (type === 'tag') {
            setPageCurrent(1)
            setTag(value)
        } else {
            $('.svgContainer').remove();
            setIsSelect(value)
            setUserId('all')
            setTag('all')
            setPageCurrent(1)
            setSchemaList([])
            setCheckList({})
            setSpinning(true)
            setSchemaId(null)
        }
    }

    function liClick(item, index) {
        if (currentSelectIndex !== index) {
            const { labelName, borderColor, color, schemaName, definition, schemaId, typeCode } = item
            window.cancelToken.forEach((ele) => {
                ele.cancel('取消请求')
            })
            window.cancelToken = [];
            $('.svgContainer').remove();
            setSchemaList([])
            setCheckList({})
            setUserId('all')
            setTag('all')
            setPageCurrent(1)
            setCurrentSelectIndex(index)
            setLabelName(labelName)
            setBorderColor(borderColor)
            setColor(color)
            setSchemaName(schemaName)
            setDefinition(definition)
            setSchemaId(schemaId)
            setTypeCode1(typeCode)
            setItemObj(item)
        }
    }

    function btnClick(type, record) {
        if (type === 'newAdd') {
            setModalVisible(true)
        } else if (type === 'preve') {
            api.goBackSchema({ taskId, litleStateCode: stateCode }).then(res => {
                window.opener.location.reload();
                window.location.href = '#/UsMakeRulesPage/taskId=' + taskId
            }).catch(err => {

            })
        } else if (type === 'delete') {
            Modal.confirm({
                title: '确认删除吗？',
                onOk() {
                    api.updateSchemaTag({
                        sampleId: record.sampleId,
                        tag: '03'
                    }).then(res => {
                        message.success('删除成功！')
                        $('.svgContainer').remove();
                        setCheckList({})
                        setSchemaList([])
                        getSchemaJsonList()
                        getSchemaList('handle')

                    }).catch(err => { })
                },
                cancelButtonProps: {
                    className: 'mds-btn-warm',
                    style: {
                        marginLeft: '8px'
                    }
                }
            })
        } else if (type === 'guideLine') {
            api.getConfirmInfo({
                taskId
            }).then(res => {
                const { data: { publishComfirm, publishState } } = res
                if (publishState === '02') {
                    window.opener.location.reload();
                    window.location.href = '#/ConfirmGuideLinePage/taskId=' + taskId
                } else {
                    setModalTitle('提示')
                    setPublishState(publishState)
                    setGuideLineVisible(true)
                    setModalContent(publishComfirm)
                }
            }).catch(err => { })
        } else if (type === 'cancel') {
            window.close()
        }
    }

    function renderSvg(item, index) {
        const len = $("#" + item.sampleId).children().length
        if (len === 0) {
            new Annotator(item.annotation, document.getElementById(item.sampleId), {
                labelWidthCalcMethod: 'max',
                unconnectedLineStyle: 'none'
            })
        }
    }

    function tagChange(record, checked, type) {
        let newList = JSON.parse(JSON.stringify(checkList))
        const { sampleId } = record
        if (type === '01' && checked || type === '02' && checked) {
            newList[sampleId] = type
        } else {
            newList[sampleId] = '00'
        }
        api.updateSchemaTag({
            sampleId,
            tag: newList[sampleId]
        }).then(res => {
            setCheckList(newList)
            getSchemaList('handle')
        }).catch(err => { })
    }

    function newAdd(type) {
        if (type === '1') {
            setLabelVisible(true)
            setEventType('new')
            setSchemaName('')
        }
    }

    function liMouseEvent(type, index) {
        if (type === 'enter') {
            setFocusIndex(index)
        } else if (type === 'leave') {
            setFocusIndex(null)
        }
    }

    function iconClick(item, type) {
        if (type === 'edit') {
            setLabelVisible(true)
            setEventType('edit')
            setColor1(item.color)
            setSchemaName(item.schemaName)
        } else if (type === 'delete') {
            Modal.confirm({
                title: '确认删除吗？',
                onOk() {
                    api.deleteSchema({
                        taskId,
                        schemaId: item.schemaId
                    }).then(res => {
                        message.success('删除成功！')
                        setSchemaList([])
                        setCheckList({})
                        setDefinition(null)
                        setColor1('#5A8BFF')
                        setCurrentSelectIndex(null)
                        getSchemaList('handle')

                    }).catch(err => { })
                },
                cancelButtonProps: {
                    className: 'mds-btn-warm',
                    style: {
                        marginLeft: '8px'
                    }
                }
            })
        }
        setIconType(type)
    }

    return (
        <div className='mds-all-layout-wrap' >
            <LayoutHeader />
            <div className='mds-executePage-wrap'>
                <div className='mds-executePage-contentWrap' style={{ display: 'flex', flex: '1', height: '100%', overflowY: 'auto' }}>
                    <Spin spinning={spinning} tip='加载中...' style={{ zIndex: '1' }}>
                        <div className='mds-executePage-leftWrap' id='tooltipWrap'>
                            <div className='mds-executePage-conditionWrapL'>
                                <Select value={typeCode} onChange={(value) => selChange(value, 'typeCode')} placeholder='请选择' style={{ minWidth: '81px', marginRight: '5px' }}>
                                    {
                                        labelTypeList.map(item => {
                                            return (
                                                <Option value={item.code}>{item.value}</Option>
                                            )
                                        })
                                    }
                                </Select>
                                <Select value={isSelect} onChange={(value) => selChange(value, 'isSel')} placeholder='请选择' style={{ width: '150px', marginRight: '5px' }}>
                                    <Option value='all'>全部</Option>
                                    <Option value='yes'>已选择</Option>
                                    <Option value='no'>未选择</Option>
                                </Select>
                                <Button type='primary' onClick={() => newAdd('1')}>新增</Button>
                            </div>
                            <ul className='mds-executePage-ulWrap'>
                                {
                                    liList.length > 0
                                        ?
                                        liList.map((item, index) => {
                                            return (
                                                <li
                                                    onMouseEnter={() => liMouseEvent('enter', index)}
                                                    onMouseLeave={() => liMouseEvent('leave', index)}
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
                                                    {
                                                        focusIndex === index
                                                            ?
                                                            <FormOutlined
                                                                className='liIcon editIcon'
                                                                onClick={() => iconClick(item, 'edit')}
                                                            />
                                                            :
                                                            null
                                                    }
                                                    {
                                                        focusIndex === index
                                                            ?
                                                            <MinusCircleOutlined
                                                                className='liIcon deleteIcon'
                                                                onClick={() => iconClick(item, 'delete')}
                                                            />
                                                            :
                                                            null
                                                    }
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
                        <div className='mds-schema-head'>
                            <label>标签定义</label>
                            <ColorPicker
                                animation="slide-up"
                                color={color}
                                onChange={(value) => colorChange(value, '0')}
                            />
                        </div>
                        <TextArea
                            value={definition}
                            onChange={textAreaChange}
                            placeholder="请输入标签定义"
                            autoSize={{ minRows: 3, maxRows: 5 }}
                        />
                        <div className='mds-schema-infoWrap'>
                            {/* <Button type='primary' onClick={() => btnClick('newAdd')}>新增</Button> */}
                            <div className='mds-schema-infoSelWrap'>
                                <Space size={'middle'}>
                                    <Select value={tag} onChange={(value) => selChange(value, 'tag')} style={{ width: "150px" }}>
                                        <Option value="all">全部</Option>
                                        <Option value="00">待处理</Option>
                                        <Option value="01">正面</Option>
                                        <Option value="02">负面</Option>
                                    </Select>
                                    <Select value={userId} onChange={(value) => selChange(value, 'user')} style={{ width: "150px" }}>
                                        {
                                            userList.map(item => {
                                                return (
                                                    <Option value={item.code}>{item.value}</Option>
                                                )
                                            })
                                        }
                                    </Select>
                                </Space>
                            </div>
                        </div>
                        {/* <div id='svgAllContainer'>
                            {
                                schemaList.map((item, index) => {
                                    return (
                                        renderSvg(item, index)
                                    )
                                })
                            }
                        </div> */}
                        <div className='mds-schema-table'>
                            <Table
                                loading={tabldLoading}
                                key={record => record.sampleId}
                                // columns={columns}
                                dataSource={schemaList}
                                scroll={{ x: 1000, y: 500 }}
                                pagination={{
                                    position: ['bottomCenter'],
                                    onChange: pageChange,
                                    current: pageCurrent,
                                    pageSize,
                                    total: pageTotal
                                }}
                            >
                                <Table.Column
                                    title='使用场景'
                                    dataIndex='annotation'
                                    key='annotation'
                                    width='900px'
                                    ellipsis='true'
                                    render={(obj, item, index) => {
                                        return (
                                            <div className='svgContainer' id={item.sampleId}></div>
                                        )
                                    }}
                                />
                                <Table.Column
                                    title='正面'
                                    dataIndex='tag01'
                                    key='tag01'
                                    width='100px'
                                    fixed='right'
                                    render={(obj, record, index) => {
                                        return (
                                            <CheckableTag
                                                className='checkIcon'
                                                checked={checkList ? (checkList[record.sampleId] === '00' || checkList[record.sampleId] === '02' ? false : true) : false}
                                                onChange={checked => tagChange(record, checked, '01')}
                                            >
                                            </CheckableTag>
                                        )
                                    }}
                                />
                                <Table.Column
                                    title='负面'
                                    dataIndex='tag02'
                                    key='tag02'
                                    width='100px'
                                    fixed='right'
                                    render={(obj, record, index) => {
                                        return (
                                            <CheckableTag
                                                className='checkIcon'
                                                checked={checkList ? (checkList[record.sampleId] === '00' || checkList[record.sampleId] === '01' ? false : true) : false}
                                                onChange={checked => tagChange(record, checked, '02')}
                                            >
                                            </CheckableTag>
                                        )
                                    }}
                                />
                                <Table.Column
                                    title='操作'
                                    dataIndex='delete'
                                    key='delete'
                                    width='100px'
                                    fixed='right'
                                    render={(obj, record, index) => {
                                        return (
                                            <Button type='primary' danger onClick={() => btnClick('delete', record)}>删除</Button>
                                        )
                                    }}
                                />
                            </Table>
                        </div>
                        <Button onClick={() => modalOk('save')} type='primary' style={{ width: '100px', alignSelf: 'flex-end' }}>保存</Button>
                    </div>

                </div>
                <div className='mds-schema-btns' >
                    <MyButton
                        label='上一步'
                        onClick={() => btnClick('preve')}
                        style={{ marginRight: '40px' }}
                    />
                    <MyButton
                        label='确认GuideLine'
                        onClick={() => btnClick('guideLine')}
                        style={{ marginRight: '40px' }}
                    />
                    <MyButton
                        label='取消'
                        onClick={() => btnClick('cancel')}
                        classType='warm'
                    />
                </div>
            </div>
            <Modal
                title="新增"
                visible={modalVisible}
                onCancel={modalCancel}
                style={{
                    top: 10
                }}
                footer={
                    <Fragment>
                        <Button type={'primary'} onClick={modalOk}>确定</Button>
                        <MyButton label='取消' classType='warm' onClick={modalCancel} />
                    </Fragment>
                }
            >
                <SelectWrap label='正负面选择' selChange={selChange} list={[]} style={{ width: '250px' }} />
                <div className='mds-schema-braftWrap'>
                    <label>使用场景：</label>
                    <ReactQuill
                        theme='bubble'
                        value={editorHtml}
                        onChange={editorChange}
                        modules={{
                            toolbar: [
                                ['underline']
                            ]
                        }}
                    />
                </div>
                <p style={{ paddingLeft: "75px" }}>（标注的实体请用<span style={{
                    textDecoration: 'underline'
                }}>实体名称</span>表示）</p>
            </Modal>
            <Modal
                title={eventType === 'edit' ? "编辑标签" : '新增标签'}
                visible={labelVisible}
                onCancel={modalCancel}
                destroyOnClose
                footer={
                    <Fragment>
                        <Button type={'primary'} onClick={() => modalOk(eventType)}>确定</Button>
                        <MyButton label='取消' classType='warm' onClick={modalCancel} />
                    </Fragment>
                }
            >
                <div className='mds-LabelDefinition-newAddModal'>
                    <Input.Group compact style={{ display: 'flex' }}>
                        <Select disabled={iconType === 'edit' ? true : false} value={typeCode1} onChange={(value) => selChange(value, 'typeSel1')} placeholder='请选择' >
                            {
                                labelTypeList.map(item => {
                                    if (item.value !== '全部') {
                                        return (
                                            <Option value={item.code}>{item.value}</Option>
                                        )
                                    }
                                })
                            }
                        </Select>
                        <Input value={schemaName} onChange={labelInputChange} />
                        {
                            eventType === 'edit'
                                ?
                                null
                                :
                                <ColorPicker
                                    animation="slide-up"
                                    color={color1}
                                    onChange={(value) => colorChange(value, '1')}
                                />
                        }
                    </Input.Group>
                </div>
            </Modal>
            <MyModal
                visible={guideLineVisible}
                // modalOk={modalOk}
                modalCancel={() => {
                    setGuideLineVisible(false)
                }}
                title={modalTitle}
                footer={
                    publishState === '01'
                        ?
                        <Button type={'primary'} onClick={() => {
                            setGuideLineVisible(false)
                        }}>
                            {'确定'}
                        </Button>
                        :
                        <Fragment>
                            <Button type={'primary'} onClick={() => modalOk('guideLine')}>确定</Button>
                            <MyButton label='取消' classType='warm' onClick={() => {
                                setGuideLineVisible(false)
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

