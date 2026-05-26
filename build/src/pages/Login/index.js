import React, { Component, Fragment } from 'react';
import { Form, Input, Button, Modal } from 'antd';
import * as api from './services'
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { connect } from 'react-redux'
import * as actionCreators from './store/actionCreators'
// import bg2 from './img/title.png'
import './index.less'

const layout = {
    labelCol: {
        span: 8,
    },
    wrapperCol: {
        span: 16,
    },
};
const tailLayout = {
    wrapperCol: {
        offset: 8,
        span: 16,
    },
};

const { encryptedString, RSAKeyPair } = window

class Index extends Component {

    state = {
        btnLoading: false
    }

    componentDidMount = () => {
        const logged = localStorage.getItem("logged");
        if (this.props.islogin || logged === "true") {
            // 进入权限路由会再次确认权限
            api.getLoginInfo().then(res => {
                if (res.retcode === '01') {
                    // this.props.history.push('/HKKS/HKKSIndex')
                    this.props.history.push('/HKKS/SentenceSegmentationTask')
                }
            }).catch(err => {
            })
        }
    }

    onFinish = values => {
        this.setState({
            btnLoading: true
        })
        const { username, password } = values
        api.login({
            username,
            password: encryptedString(new RSAKeyPair("10001", "", "b582bfab21f625687e985e19d"), password),
        }).then(res => {
            this.props.setPersonInfo(res)
            localStorage.setItem("logged", "true")
            // this.props.history.push('/HKKS/HKKSIndex')
            this.props.history.push('/HKKS/SentenceSegmentationTask')
            localStorage.setItem("userName", res.userName);
        }).catch(err => {
            this.setState({
                btnLoading: false
            })
        })
    };

    render() {
        return (
            <Fragment>
                {/* <img src={bg2} /> */}
                <div className='login-wrap'>
                    <div className='login-form-wrap'>
                        {/* <div className='login-title'>数据安全属性识别和推理原型系统</div> */}
                        {/* <div className='login-title'>雄安气象四季知识图谱</div> */}
                        <div className='login-title'>领域知识图谱构建工具</div>
                        <Form
                            name="normal_login"
                            className="login-form"
                            onFinish={this.onFinish}
                        >
                            <Form.Item
                                name="username"
                                rules={[{ required: true, message: '请输入用户名' }]}
                            >
                                <Input style={{ width: '100%' }} prefix={<UserOutlined className="site-form-item-icon" />} placeholder="用户名" />
                            </Form.Item>
                            <Form.Item
                                name="password"
                                rules={[{ required: true, message: '请输入密码!' }]}
                            >
                                <Input
                                    style={{ width: '100%' }}
                                    prefix={<LockOutlined className="site-form-item-icon" />}
                                    type="password"
                                    placeholder="密码"
                                />
                            </Form.Item>

                            <Form.Item>
                                <Button loading={this.state.btnLoading} type="primary" htmlType="submit" className="login-form-button">
                                    登录
                            </Button>
                            </Form.Item>
                        </Form>
                    </div>
                </div>
            </Fragment>

        )
    }
}

const mapStateToProps = (state) => {
    return { ...state.LoginReducer }
}

const mapDispatchToProps = (dispatch) => {
    return {
        setPersonInfo(info) {
            const action = actionCreators.setPersonInfo(info)
            dispatch(action)
        }
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(Index)
