import React, { Component, Fragment } from 'react'
import './index.less'
import { connect } from 'react-redux'
import { PoweroffOutlined, LockOutlined } from '@ant-design/icons';
import logo from './img/logo3.png'
import * as api from './services'
import { Modal, Space, message, Form, Input, Button } from 'antd';
import { MyButton } from '@/components/commonWrap';
import Axios from "axios";
import { createHashHistory } from 'history';

const history = createHashHistory();
let flag = true
const { encryptedString, RSAKeyPair } = window

class Index extends Component {

    constructor(props) {
        super(props)

        this.state = {
            visible: false
        }
    }

    formRef = React.createRef();
    clearStorage = () => {
        flag = false
        localStorage.setItem("logged", "false")
        localStorage.setItem("cookie_logged", "false")
        this.props.loginOut()
        this.props.isClickLoginOut(true)
    }
    loginOut = () => {
        const _this = this
        if (flag) {
            Modal.confirm({
                title: '确定注销吗？',
                cancelText: '取消',
                onOk() {
                    new Promise(resolve => {
                        setTimeout(() => {
                            resolve(Axios.post('/data/LoginOut.svt'))
                        }, 500)
                    }).then(res => {
                        message.success('注销成功，即将跳转', 0.5)
                        return new Promise(resolve => {
                            resolve(_this.clearStorage())
                        })
                    }).then(res => {
                        return new Promise(resolve => {
                            resolve(
                                setTimeout(() => {
                                    history.push("/login")
                                }, 1000)
                            )
                        })
                    }).then(res => {
                        Modal.destroyAll()
                        _this.props.isClickLoginOut(false)
                        flag = true
                    })
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

    returnHome = () => {
        history.push("/HKKS/SentenceSegmentationTask")
    }

    changePwd = () => {
        this.setState({
            visible: true
        })
    }

    onCancel = () => {
        this.setState({
            visible: false
        })
    }


    modalOk = () => {
        this.formRef.current.validateFields()
            .then((values) => {
                const { oldPassword, newPassword } = values
                api.changePwd({
                    oldPassword:encryptedString(new RSAKeyPair("10001", "", "b582bfab21f625687e985e19d"), oldPassword),
                    newPassword:encryptedString(new RSAKeyPair("10001", "", "b582bfab21f625687e985e19d"), newPassword)
                }).then(res=>{
                    message.success('修改成功！')
                    this.setState({
                        visible:false
                    })
                })
            })
            .catch((info) => {
                console.log('Validate Failed:', info);
            });
    }
    render() {
        return (
            <header style={this.props.style} className={'mds-layout-header'} >
                <div className='mds-headerTilte-allWrap' onClick={this.returnHome}>
                    <img width={100} height={30} src={logo} />
                    <div className='mds-headerTilte-wrap'>
                        {/* <p>气象灾害知识应用系统</p>
                        <p>Hearken Knowledge Studio</p> */}
                        {/* <p>数据安全属性识别和推理原型系统</p>
                        <p>Data Security Attribute Identification And Reasoning Prototype System</p> */}
                        <p>领域知识图谱构建工具</p>
                        <p>Hearken Knowledge Studio</p>
                    </div>
                </div>
                <div className='mds-headerTilte-loginOut' >
                    <Space align='center' size='large' >
                        <Space align='center' onClick={this.changePwd}>
                            <LockOutlined />
                            <span>修改密码</span>
                        </Space>
                        <Space align='center' onClick={this.loginOut}>
                            <PoweroffOutlined />
                            <span>注销</span>
                        </Space>
                    </Space>
                </div>
                <Modal
                    visible={this.state.visible}
                    title="修改密码"
                    destroyOnClose
                    maskClosable={false}
                    onCancel={this.onCancel}
                    footer={
                        <Fragment>
                            <Button type={'primary'} onClick={this.modalOk}>确定</Button>
                            <MyButton label='取消' classType='warm' onClick={this.onCancel} />
                        </Fragment>
                    }
                >
                    <Form
                        ref={this.formRef}
                        layout="vertical"
                        name="form_in_modal"
                    >
                        <Form.Item
                            name="oldPassword"
                            label="旧密码"
                            rules={[
                                {
                                    required: true,
                                    message: '请输入旧密码!',
                                },
                            ]}
                        >
                            <Input.Password />
                        </Form.Item>
                        <Form.Item
                            name="newPassword"
                            label="新密码"
                            dependencies={['oldPassword']}
                            hasFeedback
                            rules={[
                                {
                                    required: true,
                                    message: '请确认新密码!',
                                },
                                ({ getFieldValue }) => ({
                                    validator(rule, value) {
                                        if (!value || getFieldValue('oldPassword') !== value) {
                                            return Promise.resolve();
                                        }

                                        return Promise.reject('新密码和旧密码不能一致!');
                                    },
                                })
                            ]}
                        >
                            <Input.Password />
                        </Form.Item>
                        <Form.Item
                            name="confirmPassword"
                            label="确认新密码"
                            dependencies={['newPassword']}
                            hasFeedback
                            rules={[
                                {
                                    required: true,
                                    message: '请再次确认新密码!',
                                },
                                ({ getFieldValue }) => ({
                                    validator(rule, value) {
                                        if (!value || getFieldValue('newPassword') === value) {
                                            return Promise.resolve();
                                        }

                                        return Promise.reject('两次密码不一致!');
                                    },
                                })
                            ]}
                        >
                            <Input.Password />
                        </Form.Item>
                    </Form>
                </Modal>
            </header>
        )
    }
}

const mapStateToProps = (state) => {
    return {

    }
}

const mapDispatchToProps = (dispatch) => {
    return {
        loginOut() {
            dispatch({
                type: 'loginOut',
                payload: {
                    islogin: false,
                    userName: ''
                }
            })
        },
        isClickLoginOut(value) {
            dispatch({
                type: 'isClickLoginOut',
                payload: {
                    isClick: value
                }
            })
        }
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(Index)