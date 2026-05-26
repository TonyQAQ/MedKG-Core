/*
    author:xingge
*/
import React, { Component } from 'react';
import axios from 'axios';
import qs from 'qs';
import { message, notification, Modal } from 'antd';
import { baseUrl1, baseUrl2, baseUrl3 } from "./baseUrl"
import { createHashHistory } from 'history';

const history = createHashHistory();
axios.defaults.timeout = 30000; // 超时设置,超时进入错误回调，进行相关操作
axios.defaults.withCredentials = false; // 是否支持跨域cookie
window.cancelToken = [];
let sources = {}

axios.interceptors.response.use(response => {
    if (response.hasOwnProperty('data') && response.data.retcode === '00') {
        if (response.data.retmsg === '用户未登陆！' && response.config.url.indexOf('isEmpty') < 0 && window.location.href.indexOf('Login') < 0) {
            // Modal.destroyAll();
            // Modal.error({
            //     title: '用户未登录或者登录状态过期！',
            //     onOk() {
            //         history.push("/Login")

            //     }
            // });
            window.cancelToken.forEach((ele) => {
                ele.cancel('取消请求')
            })
            window.cancelToken = [];
            return Promise.reject('登录超时')
        } else {
            if (response.config.url.indexOf('isEmpty') > -1 || response.data.retmsg === '退出失败!') {
                window.cancelToken.forEach((ele) => {
                    ele.cancel('取消请求')
                })
                window.cancelToken = [];
                return response.data
            } else {
                message.destroy()
                return Promise.reject(message.error(response.data.retmsg || response.retmsg))
            }
        }
    } else if (response.hasOwnProperty('data') && response.data.retcode === '01') {
        return response.data
    } else if (response.status === 200) {
        return response.data
    }
}, err => {
    // 异常处理
    const { config, code } = err
    let status
    if (err.response) {
        status = err.response.status
    }
    if (code === 'ECONNABORTED' || err.message === 'Network Error' || status === 500) { // 请求超时
        message.destroy()
        message.error(codeMessage[status] || err.response.statusText)
        throw err
    }
    // 可以进行相关提示等处理
    // throw err
})

axios.interceptors.request.use((config) => {
    if (!(config.url.indexOf('isEmpty') > -1)) {
        config.cancelToken = new axios.CancelToken((cancel) => {
            window.cancelToken.push({ cancel })
            // sources[request] = cancel
        })
    }
    if (config.url.indexOf('LoginOut.svt') > -1 || config.url.indexOf('Login.svt') > -1 || config.url.indexOf('fileLoad.svt') > -1) {
        config.baseURL = baseUrl1
    } else if (config.url.indexOf('knowledge') > -1) {
        config.baseURL = baseUrl3
    } else {
        config.baseURL = baseUrl2
    }
    return config
});


const codeMessage = {
    200: '服务器成功返回请求的数据',
    201: '新建或修改数据成功。',
    202: '一个请求已经进入后台排队（异步任务）',
    204: '删除数据成功。',
    400: '发出的请求有错误，服务器没有进行新建或修改数据,的操作。',
    401: '用户没有权限（令牌、用户名、密码错误）。',
    403: '用户得到授权，但是访问是被禁止的。',
    404: '发出的请求针对的是不存在的记录，服务器没有进行操作',
    406: '请求的格式不可得。',
    410: '请求的资源被永久删除，且不会再得到的。',
    422: '当创建一个对象时，发生一个验证错误。',
    500: '服务器发生错误，请检查服务器',
    502: '网关错误',
    503: '服务不可用，服务器暂时过载或维护',
    504: '网关超时',
};

function checkStatus(response) {
    if (response.status >= 200 && response.status < 300) {
        return response;
    }
    const errortext = codeMessage[response.status] || response.statusText;
    // 提示框
    notification.error({
        message: `请求错误 ${response.status}: ${response.url}`,
        description: errortext,
    });
    const error = new Error(errortext || '请求错误');
    error.name = response.status;
    error.response = response;
    throw error;
}

export default function request(url, options) {
    const defaultOptions = {
        credentials: 'include',
    };
    const newOptions = { ...defaultOptions, ...options };
    if (
        newOptions.method === 'POST' ||
        newOptions.method === 'PUT' ||
        newOptions.method === 'DELETE'
    ) {
        if (newOptions.isUpload) {
            newOptions.headers = {
                "Content-Type": "multipart/form-data",
                ...newOptions.headers,
            };
            newOptions.data = newOptions.body.formData
        } else if (!newOptions.isForm && newOptions.method === 'POST') {
            newOptions.headers = {
                Accept: 'application/json',
                "Content-Type": 'application/x-www-form-urlencoded;charset=UTF-8',
                ...newOptions.headers,
            };
            newOptions.data = qs.stringify(newOptions.body);
        } else {
            newOptions.headers = {
                Accept: 'application/json',
                "Content-Type": 'application/json;charset=UTF-8',
                ...newOptions.headers,
            };
            newOptions.data = JSON.stringify(newOptions.body);
        }
    }
    if (Array.isArray(url)) {
        return axios.all(url)
            // .then(checkStatus)
            .then(axios.spread(function (...res) {
                return res
            }))
            .catch((e) => {
                return Promise.reject(e)
            })
    }
    return axios(url, newOptions)
}
