import React, { Component } from 'react';
import { withRouter } from 'react-router';
import { Route, useHistory } from 'react-router-dom';
import { connect } from 'react-redux';
import { message, Modal, Spin } from "antd";
import * as api from "./services";
import { createHashHistory } from 'history';
import { LoadingOutlined } from '@ant-design/icons';
import './index.less'

const antIcon = <LoadingOutlined style={{ fontSize: 48 }} spin />;

const history = createHashHistory();

class AuthorizedRoute extends Component {
    constructor(props) {
        super(props)

        this.state = {
            isLogged: 0,
            count: 1,
            isTrucAction: null
        }
    }

    componentDidMount = () => {
        this.getLoginInfo(1, false);
    };

    componentWillReceiveProps = (nextProps, props) => {
        if (nextProps.location.pathname !== this.props.location.pathname) {
            this.getLoginInfo(1, false);
        }
    };

    getLoginInfo = (count, isClick) => {
        const _this = this
        if (count === 1 && isClick === false) {
            const { isLogged } = _this.state
            api.getLoginInfo().then(res => {
                const { retcode } = res;
                if (retcode === '00') {
                    message.destroy();
                    message.config({
                        maxCount: 1,
                        top: -999
                    });

                    Modal.error({
                        title: '用户未登录或者登录状态过期！',
                        onOk() {
                            Modal.destroyAll();
                            history.push("/Login");
                            _this.props.backLogin()
                            localStorage.setItem('logged', false)
                        },
                    });

                } else {
                    const { userName, roleId } = res;
                    message.config({
                        top: 24,
                        maxCount: 3
                    });
                    this.setState({
                        isLogged: 1
                    }, () => {
                        this.props.setHeader(userName, roleId);
                        const { taskId } = this.props.computedMatch.params
                        const { pageType, littleStateCode, stateCode, littleStateCodeArry } = this.props
                        if (pageType !== '03' && pageType !== 'NLP') {
                            api.getTaskStateInfo({
                                taskId, pageType
                            }).then(res => {
                                if (littleStateCodeArry ?
                                    ((littleStateCode.indexOf(res.data.littleStateCode) === -1 ? true : false) || (stateCode.indexOf(res.data.stateCode) === -1 ? true : false)) :
                                    (res.data.littleStateCode !== littleStateCode || res.data.stateCode !== stateCode)
                                ) {
                                    this.setState({
                                        isTrucAction: false
                                    }, () => {
                                        Modal.error({
                                            title: '当前任务状态与页面状态不匹配！',
                                            onOk() {
                                                window.location.replace('#/HKKS/DataSourceMng')
                                                window.location.reload()
                                            }
                                        })
                                    })
                                } else {
                                    this.setState({
                                        isTrucAction: true
                                    })
                                }
                            }).catch(err => { })
                        } else {
                            this.setState({
                                isTrucAction: true
                            })
                        }
                    });
                }
            }).catch(err => {
                message.error('服务器问题，请联系管理员！')
            })
        }
    };

    render() {
        const { component: Component, ...rest } = this.props;
        const { isLogged, isTrucAction } = this.state;
        const _props = this.props;
        return (
            <Route {...rest} render={props => {
                return isLogged === 1 ? (isTrucAction ? <Component {...props} /> : null) : <Spin indicator={antIcon} className='spin-wrap' />;
            }} />
        );
    }
}

const stateToProps = (state) => {
    return { ...state.LoginReducer }
};
const dispatchToProps = (dispatch) => ({
    loginIn() {
        dispatch({
            type: 'loginIn',
            payload: {
                islogin: true
            }
        });
    },
    backLogin() {
        dispatch({
            type: 'backLogin',
            payload: {
                islogin: false
            }
        });
    },
    setHeader(userName, roleId) {
        dispatch({
            type: 'loginSetPersonInfo',
            payload: {
                info: {
                    userName, roleId
                }
            }
        });
    }
});

export default connect(stateToProps, dispatchToProps)(withRouter(AuthorizedRoute));
