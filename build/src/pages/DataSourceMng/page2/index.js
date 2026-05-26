import React, { useEffect, useState } from 'react'
import { connect } from 'react-redux'
import PanelTitle from '@/components/PanelTitle'
import { MyButton } from '@/components/commonWrap'
import { Form, Input, Select, Spin, message, Modal } from 'antd';
import debounce from 'lodash/debounce';
import * as actionCreators from '../store/actionCreators'
import * as api from '../services'

const { CryptoJS } = window

function decrypt(word) {
    const key = CryptoJS.enc.Utf8.parse("HearKenKnowledge");
    const decrypt = CryptoJS.AES.decrypt(word, key, { mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7 });
    return CryptoJS.enc.Utf8.stringify(decrypt).toString();
}

export const Index = (props) => {
    // 方法
    const { newAddOrCancel } = props

    const [form] = Form.useForm();
    const [dataTypeInfoList, setDataTypeInfoList] = useState([])
    const [isOracle, setIsOracle] = useState(false)
    const [serviceType, setServiceType] = useState([])
    const [driver, setDriver] = useState('')
    const [formSpin, setFormSpin] = useState(false)
    const [formTip, setFormTip] = useState('')
    const [connectionId, setConnectionId] = useState('')
    const [isEdit, setIsEdit] = useState(false)
    const [isXuGu, setIsXuGu] = useState(false)



    useEffect(() => {
        const { isEdit, editInfo } = props
        api.getSourceTypeInfo().then(res => {
            setDataTypeInfoList(res.data)
        }).catch(err => { })
        if (isEdit) {
            const { connectionId } = editInfo
            api.getDataSourceList({
                connectionId
            }).then(res => {
                const {
                    connectionName,
                    ip,
                    port,
                    databaseName,
                    databaseType,
                    username,
                    password,
                    serviceType,
                    serviceOther,
                    memo,
                    driver
                } = res.data[0]
                setDriver(driver)
                setIsEdit(true)
                setConnectionId(connectionId)
                form.setFieldsValue({
                    connectionName,
                    ip,
                    port,
                    databaseName,
                    databaseType,
                    username,
                    password: decrypt(password),
                    serviceType,
                    serviceOther,
                    memo
                })
            }).catch(err => { })
        } else {
            const formObj = JSON.parse(localStorage.getItem('dataSourceMngAddObj'))
            const driver = localStorage.getItem('driver')
            if (formObj) {
                Modal.confirm({
                    title: '系统检测到你上次填写的表单未进行保存，是否继续填写？',
                    onOk() {
                        const isOracle = localStorage.getItem('isOracle')
                        const isXuGu = localStorage.getItem('isXuGu')
                        setIsOracle(isOracle ? (isOracle === 'false' ? false : true) : false)
                        setIsXuGu(isXuGu ? (isXuGu === 'false' ? false : true) : false)
                        setDriver(driver)
                        form.setFieldsValue(formObj)
                    },
                    onCancel() {
                        removeItem()
                    },
                    cancelButtonProps: {
                        className: 'mds-btn-warm',
                        style: {
                            marginLeft: '8px'
                        }
                    }
                })
            }
        }
    }, [])

    const removeItem = () => {
        localStorage.removeItem('dataSourceMngAddObj')
        localStorage.removeItem('isOracle')
        localStorage.removeItem('isXuGu')
        localStorage.removeItem('driver')
    }

    // 测试按钮事件
    const onFinish = fieldsValue => {
        // Should format date value before submit.
        setFormSpin(true)
        setFormTip('测试连接中...')
        const { connectionName, databaseName, databaseType, ip, password, port, serviceType, username, memo, serviceOther } = fieldsValue
        const key = CryptoJS.enc.Utf8.parse("HearKenKnowledge");
        const srcs = CryptoJS.enc.Utf8.parse(password);
        const encrypted = CryptoJS.AES.encrypt(srcs, key, { mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7 })
        api.testLink({
            connectionName,
            databaseName,
            databaseType,
            ip,
            password: encrypted.toString(),
            port,
            username,
            driver,
            serviceType,
            memo,
            serviceOther
        }).then(res => {
            message.success(res.retmsg)
            setFormSpin(false)
        }).catch(err => {
            setFormSpin(false)
        })
    };


    // 类型选择事件
    const databaseTypeSel = (value, { record }) => {
        const { ip, port, databaseName, serviceType, username, driver } = record
        if (value === 'Oracle') {
            setServiceType(serviceType)
            setIsOracle(true)
            setIsXuGu(false)
            localStorage.setItem('isOracle', true)
            localStorage.setItem('isXuGu', false)
        } else if (value === 'Xugu') {
            setIsOracle(false)
            setIsXuGu(true)
            localStorage.setItem('isOracle', false)
            localStorage.setItem('isXuGu', true)
        } else {
            setIsOracle(false)
            setIsXuGu(false)
            localStorage.setItem('isOracle', false)
            localStorage.setItem('isXuGu', false)
        }
        form.setFieldsValue({
            ip, port, databaseName, username
        })
        setDriver(driver)
        localStorage.setItem('driver', driver)
    }


    // 保存按钮事件
    const saveClick = () => {
        const { connectionName, databaseName, databaseType, ip, password, port, serviceType, username, memo, serviceOther } = form.getFieldValue()
        function judgeSave(object) {
            let result = false
            for (const key in object) {
                if (object[key] === undefined || object[key] === '') {
                    result = true
                }
            }
            return result
        }
        const params = isOracle
            ?
            {
                connectionName, databaseName, databaseType, ip, password, port, username, serviceType
            }
            :
            {
                connectionName, databaseName, databaseType, ip, password, port, username
            }
        if (judgeSave(params)) {
            form.validateFields()
        } else {
            const key = CryptoJS.enc.Utf8.parse("HearKenKnowledge");
            const srcs = CryptoJS.enc.Utf8.parse(password);
            const encrypted = CryptoJS.AES.encrypt(srcs, key, { mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7 })
            setFormSpin(true)
            setFormTip('保存中...')
            api.saveOrEditLink({
                connectionName,
                databaseName,
                databaseType,
                ip,
                password: encrypted.toString(),
                port,
                username,
                driver,
                memo,
                serviceOther,
                connectionId: isEdit ? connectionId : null
            }).then(res => {
                removeItem()
                message.success(res.retmsg)
                newAddOrCancel(1)
            }).catch(err => {
                setFormSpin(false)
            })
        }
    }

    const onValuesChange = debounce((changedValues, allValues) => {
        localStorage.setItem('dataSourceMngAddObj', JSON.stringify(form.getFieldsValue()))
    }, 200)


    const config = {
        rules: [{ required: true, max: 20 }]
    }
    return (
        <div className='mds-dataSource-page2Warp'>
            <PanelTitle title='基本信息' />
            <Spin spinning={formSpin} tip={formTip}>
                <div style={{
                    maxWidth: '960px',
                    padding: '20px 35px'
                }}>
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
                        <Form.Item label="数据源名称" name='connectionName' {...config}>
                            <Input placeholder='请输入数据源名称' />
                        </Form.Item>
                        <div id='databaseType' style={{ position: 'relative' }}>
                            <Form.Item style={{ position: 'relative' }} label="类型" name='databaseType' rules={[{ required: true, message: '请选择类型' }]}>
                                <Select getPopupContainer={() => document.getElementById('databaseType')} placeholder='请选择类型' onChange={databaseTypeSel}>
                                    {
                                        dataTypeInfoList.map(item => {
                                            return (
                                                <Select.Option key={item.code} value={item.code} record={item}>
                                                    {item.value}
                                                </Select.Option>
                                            )
                                        })
                                    }
                                </Select>
                            </Form.Item>
                        </div>
                        <Form.Item label="IP地址" name='ip' rules={[
                            {
                                required: true, max: 30,
                            }
                        ]}>
                            <Input placeholder='请输入IP地址' />
                        </Form.Item>
                        <Form.Item label="端口号" name='port' {...config}>
                            <Input placeholder='请输入端口号' />
                        </Form.Item>
                        <Form.Item label="实例名" name='databaseName' {...config}>
                            <Input placeholder='请输入实例名' />
                        </Form.Item>
                        <Form.Item label="用户名" name='username' {...config}>
                            <Input placeholder='请输入用户名' />
                        </Form.Item>
                        <Form.Item label="密码" name='password' {...config}>
                            <Input.Password placeholder='请输入密码' />
                        </Form.Item>
                        {
                            isOracle ?
                                <div
                                    id='serviceType'
                                    style={{ position: 'relative' }}
                                >
                                    <Form.Item
                                        label="服务类型"
                                        name='serviceType'
                                        rules={[{ required: true, message: '请选择服务类型' }]}>
                                        <Select getPopupContainer={() => document.getElementById('serviceType')} placeholder='请选择服务类型'>
                                            {
                                                serviceType.map(item => {
                                                    return (
                                                        <Select.Option value={item} key={item} >
                                                            {item}
                                                        </Select.Option>
                                                    )
                                                })
                                            }
                                        </Select>
                                    </Form.Item>
                                </div>
                                :
                                null
                        }
                        {
                            isOracle || isXuGu
                                ?
                                <Form.Item label="特殊参数" name='serviceOther' rules={[{ max: 20 }]}>
                                    <Input placeholder='请输入特殊参数' />
                                </Form.Item>
                                :
                                null
                        }

                        <Form.Item label="描述" name='memo' rules={[{ max: 200 }]}>
                            <Input.TextArea
                                autoSize={{ minRows: 5, maxRows: 8 }}
                                placeholder='请输入描述' />
                        </Form.Item>
                        <Form.Item
                            wrapperCol={{
                                xs: { span: 24, offset: 0 },
                                sm: { span: 16, offset: 8 },
                            }}
                        >
                            <div style={{ marginTop: '80px' }}>
                                <MyButton label='测试' htmlType="submit" classType='green' />
                                <MyButton label='保存' onClick={saveClick} style={{ margin: '0 40px' }} />
                                <MyButton label='取消' classType='warm' onClick={() => {
                                    removeItem()
                                    newAddOrCancel(1)
                                }
                                } />
                            </div>
                        </Form.Item>
                    </Form>
                </div>
            </Spin>
        </div >
    )
}

const mapStateToProps = (state) => {
    return { ...state.DataSourceMngReducer }
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
