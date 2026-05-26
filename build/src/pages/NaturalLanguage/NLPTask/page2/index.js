import React, { useEffect, useState, Fragment } from 'react'
import { connect } from 'react-redux'
import PanelTitle from '@/components/PanelTitle'
import { Form, Input, Select, DatePicker, Spin, message, Modal, Upload, Space, Tabs, Tag, Table } from 'antd';
import { MyButton } from '@/components/commonWrap'
import * as actionCreators from '../store/actionCreators'
import { InboxOutlined, PaperClipOutlined } from '@ant-design/icons';
import debounce from 'lodash/debounce';
import * as api from '../../services'
import moment from 'moment';
import '../../index.less'

const { RangePicker } = DatePicker;
const { TabPane } = Tabs
const { CheckableTag } = Tag

export const Index = (props) => {
    // 方法
    const { newAddOrCancel } = props

    const [form] = Form.useForm();
    const [taskTypeList, setTaskTypeList] = useState([])
    const [formSpin, setFormSpin] = useState(false)
    const [formTip, setFormTip] = useState('')
    const [operatorsList, setOperatorsList] = useState([])
    const [dataSourceCodeTable, setDataSourceCodeTable] = useState([])
    const [taskId, setTaskId] = useState('')
    const [stateCode, setStateCode] = useState('')
    const [editState, setEditState] = useState('')
    const [typeCode, setTypeCode] = useState(null)
    const [upLoadingFileList, setUpLoadingFileList] = useState([])
    const [isUploading, setIsUploading] = useState(false)
    const [isCheck, setIsCheck] = useState(false)
    const [ulList, setUlList] = useState([])
    const [tabKey, setTabKey] = useState('1')
    const [selectedTags, setSelectedTags] = useState([])
    const [tagsData, setTagsData] = useState([])
    const [title, setTitle] = useState([])
    const [dataSource, setDataSource] = useState([])
    const [selectedRowKeys, setSelectedRowKeys] = useState([])
    const [fromTaskId, setFromTaskId] = useState('')
    const [fromTaskType, setFromTaskType] = useState('')

    useEffect(() => {
        const { isEdit, editInfo, typeCode } = props
        setTypeCode(typeCode)
        let tagsData = []
        switch (typeCode) {
            case 'WST':
            case 'CCT':
            case 'MCT':
                setTagsData([{ label: '句子切分', value: 'SST' }])
                setSelectedTags(['SST'])
                tagsData = [{ label: '句子切分', value: 'SST' }]
                break;
            case 'EET':
                setTagsData([{ label: '句子切分', value: 'SST' }])
                setSelectedTags(['SST'])
                tagsData = [{ label: '句子切分', value: 'SST' }]
                break;
            case 'RET':
                setTagsData([{ label: '句子切分', value: 'SST' }, { label: '实体抽取', value: 'EET' }])
                setSelectedTags(['SST'])
                tagsData = [{ label: '句子切分', value: 'SST' }, { label: '实体抽取', value: 'EET' }]
                break;
            default:
                break;
        }
        if (isEdit) {
            const { taskId } = editInfo
            if (typeCode) {
                api.getEditInfo({
                    taskId
                }).then(res => {
                    const { startTime, endTime, memo, files, taskId, taskName, fromTaskId, fromTaskType } = res.data
                    if (fromTaskId) {
                        setTabKey('2')
                        setSelectedTags([fromTaskType])
                        setFromTaskId(fromTaskId)
                    }
                    setTaskId(taskId)
                    setUlList(files.map((item, index) => {
                        return {
                            uid: index,
                            name: item,
                            status: 'done',
                        }
                    }))
                    setSelectedRowKeys([fromTaskId])
                    form.setFieldsValue({
                        taskName,
                        memo,
                        rangeTimePicker: [moment(startTime), moment(endTime)],
                        // upload: files.map((item, index) => {
                        //     return {
                        //         uid: index,
                        //         name: item,
                        //         status: 'done',
                        //     }
                        // })
                    })
                }).catch(err => {

                })
            } else {
                api.getTableList({
                    taskId
                }).then(res => {
                    const {
                        connectionId,
                        endTime,
                        memo,
                        startTime,
                        taskName,
                        typeCode,
                        userIds,
                        stateCode,
                        editState
                    } = res.data[0]
                    setTaskId(taskId)
                    setStateCode(stateCode)
                    setEditState(editState)
                    form.setFieldsValue({
                        connectionId,
                        taskName,
                        typeCode,
                        memo,
                        rangeTimePicker: [moment(startTime), moment(endTime)],
                        operators: userIds.split(',')
                    })
                }).catch(err => { })
            }
        } else {
            let formObj = JSON.parse(localStorage.getItem('newAddObj'))
            if (formObj && formObj.upload) {
                delete formObj.upload
            }
            if (JSON.stringify(formObj) === "{}") {
                formObj = null
            }
            if (formObj && formObj.typeCode === typeCode) {
                let params = {}
                Modal.confirm({
                    title: '系统检测到你上次填写的表单未进行保存，是否继续填写？',
                    onOk() {
                        for (const key in formObj) {
                            params[key] = (key === 'rangeTimePicker' ? [moment(formObj[key][0]), moment(formObj[key][1])] : formObj[key])
                        }
                        form.setFieldsValue(params)
                    },
                    onCancel() {
                        removeItem()
                    },
                    cancelButtonProps: {
                        className: 'mds-btn-warm',
                        style: {
                            marginLeft: '8px'
                        }
                    },
                })
            }
        }
    }, [])

    useEffect(() => {
        if (tabKey === '2' || selectedTags.length > 0) {
            api.taskTables({
                typeCode: selectedTags.toString()
            }).then(res => {
                setDataSource(res.data)
                setTitle(res.titleHead)
            }).catch(err => { })
        }
    }, [tabKey, selectedTags])


    // 测试按钮事件
    const onFinish = fieldsValue => {
        // Should format date value before submit.
        setFormSpin(true)
        setFormTip('保存中...')
        const { taskName, connectionId, memo, rangeTimePicker, operators } = fieldsValue
        if (typeCode === 'WST' || typeCode === 'SST' || typeCode === 'EET' || typeCode === 'RET' || typeCode === 'CCT' || typeCode === 'MCT') {
            const { upload } = fieldsValue
            const formData = new FormData();
            upload && upload.map(item => {
                formData.append('files', item.originFileObj);
            })
            api.addMarkTask(props.isEdit ? (upLoadingFileList.length > 0 ? { formData } : null) : { formData }, {
                taskName,
                memo: memo ? memo : '',
                typeCode,
                startTime: rangeTimePicker[0].format('YYYY-MM-DD HH:mm'),
                endTime: rangeTimePicker[1].format('YYYY-MM-DD HH:mm'),
                taskId: props.isEdit ? taskId : '',
                fromTaskId: typeCode === 'SST' ? '' : (fieldsValue.quote ? fieldsValue.quote : ''),
                isStandard: ''
            }).then(res => {
                removeItem()
                message.success('操作成功！')
                newAddOrCancel(1)
            }).catch(err => {
                setFormSpin(false)
            })
        } else {
            api.addOrEdit({
                taskName,
                typeCode,
                connectionId,
                memo: memo ? memo : '',
                startTime: moment(rangeTimePicker[0].format('YYYY-MM-DD HH:mm')).valueOf(),
                endTime: moment(rangeTimePicker[1].format('YYYY-MM-DD HH:mm')).valueOf(),
                taskId: props.isEdit ? taskId : null,
                stateCode: props.isEdit ? stateCode : null
            }, operators.toString()).then(res => {
                removeItem()
                message.success(res.retmsg)
                newAddOrCancel(1)
            }).catch(err => {
                setFormSpin(false)
            })
        }
    };


    const onValuesChange = debounce((changedValues, allValues) => {
        let formObj = form.getFieldsValue()
        formObj.typeCode = typeCode
        localStorage.setItem('newAddObj', JSON.stringify(formObj))
    }, 200)


    const removeItem = () => {
        localStorage.removeItem('newAddObj')
    }

    const config = {
        rules: [{ required: true, max: 20 }]
    }
    const isDisabled = {
        disabled: props.isEdit ? (editState === '01' ? true : false) : false
    }
    const normFile = e => {
        const { file, fileList } = e
        if (
            file.type !== 'application/vnd.ms-excel'
            && file.type !== 'text/plain'
            && file.type !== 'application/zip'
            && file.type !== 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
            && file.type !== 'application/msword'
        ) {
            return e && upLoadingFileList
        } else if (Array.isArray(e)) {
            return e
        } else {
            return e && e.fileList;
        }

    };
    const uploadProps = {
        name: 'file',
        onChange(info) {
            const { status } = info.file;
            if (status === 'removed') {
                setUpLoadingFileList(info.fileList)
            }
        },
        beforeUpload(file, fileList) {
            const { type } = file
            if (
                type !== 'application/vnd.ms-excel'
                && type !== 'text/plain'
                && type !== 'application/zip'
                && type !== 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
                && type !== 'application/msword'
            ) {
                message.error('上传文件格式错误！')
            } else {
                setIsCheck(true)
                setUpLoadingFileList([...upLoadingFileList, file])
            }
            return false
        },
    };

    const tabChange = (value) => {
        setTabKey(value)
        if (!props.isEdit) {
            setSelectedRowKeys([])
            form.setFieldsValue({
                quote: null,
                fileList: []
            })
        } else {
            form.setFieldsValue({
                quote: selectedRowKeys[0],
                fileList: []
            })
        }
    }

    const tagChange = (tag, checked) => {
        setSelectedTags(tag.value)
        // if (!props.isEdit) {

        //     setSelectedRowKeys([])
        //     setFromTaskId(null)
        //     form.setFieldsValue({
        //         quote: ''
        //     })
        // }
    }

    const rowSelectionChange = (selectedRowKeys, selectedRows) => {
        setSelectedRowKeys(selectedRowKeys)
        setFromTaskId(selectedRowKeys[0])
        form.setFieldsValue({
            quote: selectedRowKeys[0]
        })
    }

    return (
        <div className='mds-dataSource-page2Warp'>
            <Spin spinning={formSpin} tip={formTip}>
                <Form
                    form={form}
                    onFinish={onFinish}
                    onValuesChange={props.isEdit ? null : onValuesChange}
                    labelCol={{ span: 4 }}
                    wrapperCol={{ span: 14 }}
                    layout="horizontal"
                    id='form_wrap'
                    labelAlign='left'
                >
                    <PanelTitle title='基本信息' />
                    <div style={{
                        maxWidth: '960px',
                        padding: '20px 35px'
                    }}>
                        <Form.Item label="任务名称" name='taskName' {...config}>
                            <Input placeholder='请输入任务名称' />
                        </Form.Item>
                        <div id='timePicker' style={{ position: 'relative' }}>
                            <Form.Item name="rangeTimePicker" label="时间设置" rules={[{ required: true, message: '请选择时间' }]}>
                                <RangePicker
                                    style={{ width: '100%' }}
                                    getPopupContainer={() => document.getElementById('timePicker')}
                                    showTime={{ format: 'HH:mm' }}
                                    format="YYYY-MM-DD HH:mm"
                                    placeholder={['开始时间', '结束时间']}
                                    disabledDate={props.isEdit
                                        ?
                                        (current) => {
                                            return current < moment();
                                        }
                                        :
                                        null
                                    }
                                />
                            </Form.Item>
                        </div>

                        <Form.Item label="任务简介" name='memo' rules={[{ max: 200 }]}>
                            <Input.TextArea
                                autoSize={{ minRows: 5, maxRows: 8 }}
                                placeholder='请输入任务简介' />
                        </Form.Item>
                    </div>
                    <Fragment>
                        <PanelTitle title='数据源' />
                        {
                            props.typeCode === 'SST'
                                ?
                                <div style={{
                                    maxWidth: '960px',
                                    padding: '20px 35px'
                                }}>
                                    <div id='operators' style={{ position: 'relative' }}>
                                        <Form.Item
                                            name="upload"
                                            label="上传"
                                            valuePropName="fileList"
                                            getValueFromEvent={normFile}
                                            rules={[{ required: props.isEdit ? (isCheck ? true : false) : true, message: '请上传文件' }]}
                                        >
                                            <Upload.Dragger
                                                {...uploadProps}
                                                fileList={upLoadingFileList}
                                                disabled={isUploading}
                                            // showUploadList={{
                                            //     showRemoveIcon: props.isEdit ? false : true
                                            // }}
                                            >
                                                <p className="ant-upload-drag-icon">
                                                    <InboxOutlined />
                                                </p>
                                                <p className="ant-upload-text">点击上传文件</p>
                                                <p className="ant-upload-hint">
                                                    支持的格式：CSV、TXT、DOC、DOCX、ZIP
                                    </p>
                                                {/* <Space>
                                        <a onClick={(e) => downLoadFileClick(e, 1)}>下载模版CSV</a>
                                        <a onClick={(e) => downLoadFileClick(e, 2)}>下载模版TXT</a>
                                    </Space> */}
                                            </Upload.Dragger>
                                        </Form.Item>
                                        {
                                            isCheck
                                                ?
                                                null
                                                :
                                                <ul style={{
                                                    position: 'absolute',
                                                    left: '150px',
                                                    height: '70px',
                                                    overflowY: 'auto'
                                                }}>
                                                    {
                                                        ulList.map(item => {
                                                            return (
                                                                <li>
                                                                    <Space>
                                                                        <PaperClipOutlined /><span>{item.name}</span>
                                                                    </Space>
                                                                </li>
                                                            )
                                                        })
                                                    }
                                                </ul>
                                        }
                                    </div>
                                </div>
                                :
                                <div style={{
                                    maxWidth: '960px',
                                    padding: '20px 35px'
                                }}>
                                    <Tabs onChange={tabChange} activeKey={tabKey}>
                                        {
                                            props.isEdit && fromTaskId
                                                ?
                                                null
                                                :
                                                <TabPane tab="文件上传" key="1" disabled={fromTaskId ? true : false}>
                                                    <div id='operators' style={{ position: 'relative' }}>
                                                        {
                                                            tabKey === '1'
                                                                ?
                                                                <Form.Item
                                                                    name="upload"
                                                                    label="上传"
                                                                    valuePropName="fileList"
                                                                    getValueFromEvent={normFile}
                                                                    rules={[{ required: props.isEdit ? (isCheck ? true : false) : true, message: '请上传文件' }]}
                                                                >
                                                                    <Upload.Dragger
                                                                        {...uploadProps}
                                                                        fileList={upLoadingFileList}
                                                                        disabled={isUploading}
                                                                    // showUploadList={{
                                                                    //     showRemoveIcon: props.isEdit ? false : true
                                                                    // }}
                                                                    >
                                                                        <p className="ant-upload-drag-icon">
                                                                            <InboxOutlined />
                                                                        </p>
                                                                        <p className="ant-upload-text">点击上传文件</p>
                                                                        <p className="ant-upload-hint">
                                                                            支持的格式：CSV、TXT、DOC、DOCX、ZIP
                                                   </p>
                                                                        {/* <Space>
                                                       <a onClick={(e) => downLoadFileClick(e, 1)}>下载模版CSV</a>
                                                       <a onClick={(e) => downLoadFileClick(e, 2)}>下载模版TXT</a>
                                                   </Space> */}
                                                                    </Upload.Dragger>
                                                                </Form.Item>
                                                                :
                                                                null
                                                        }
                                                        {
                                                            isCheck
                                                                ?
                                                                null
                                                                :
                                                                <ul style={{
                                                                    position: 'absolute',
                                                                    left: '150px',
                                                                    height: '70px',
                                                                    overflowY: 'auto'
                                                                }}>
                                                                    {
                                                                        ulList.map(item => {
                                                                            return (
                                                                                <li>
                                                                                    <Space>
                                                                                        <PaperClipOutlined /><span>{item.name}</span>
                                                                                    </Space>
                                                                                </li>
                                                                            )
                                                                        })
                                                                    }
                                                                </ul>
                                                        }
                                                    </div>

                                                </TabPane>
                                        }
                                        {
                                            props.isEdit && !fromTaskId > 0
                                                ?
                                                null
                                                :
                                                <TabPane tab="任务引用" key="2" disabled={upLoadingFileList.length > 0 ? true : false}>
                                                    {
                                                        tabKey === '2'
                                                            ?
                                                            <Form.Item
                                                                name="quote"
                                                                label="引用"
                                                                rules={[{ required: true, message: '请选择引用' }]}

                                                            >
                                                                {tagsData.map(tag => (
                                                                    <CheckableTag
                                                                        key={tag.value}
                                                                        checked={selectedTags.indexOf(tag.value) > -1}
                                                                        onChange={checked => tagChange(tag, checked)}
                                                                    >
                                                                        {tag.label}
                                                                    </CheckableTag>
                                                                ))}
                                                                <Table
                                                                    style={{ marginTop: '5px' }}
                                                                    rowKey='rowKey'
                                                                    columns={title}
                                                                    dataSource={dataSource}
                                                                    bordered
                                                                    rowSelection={{
                                                                        selectedRowKeys,
                                                                        onChange: rowSelectionChange,
                                                                        type: 'radio'
                                                                    }}
                                                                />
                                                            </Form.Item>
                                                            :
                                                            null
                                                    }

                                                </TabPane>
                                        }

                                    </Tabs>
                                </div>
                        }

                    </Fragment>

                    <Form.Item
                        wrapperCol={{
                            xs: { span: 24, offset: 12 },
                            sm: { span: 16, offset: 10 },
                        }}
                    >
                        <div style={{ marginTop: '80px' }}>
                            <MyButton label='保存' htmlType="submit" style={{ margin: '0 20px' }} />
                            <MyButton label='取消' classType='warm' onClick={() => {
                                removeItem()
                                newAddOrCancel(1)
                            }} />
                        </div>
                    </Form.Item>
                </Form>
            </Spin>
        </div >
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
        }
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(Index)
