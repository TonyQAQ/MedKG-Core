import request from '@/util/request';

//level1-表查询
export async function getTable(params) {
    return request('/task/us/fm/exe/search/tables', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//level2-表字段查询
export async function getFieldList(params) {
    return request('/task/us/fm/exe/search/columns', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//获取用户表
export async function getUsers(params) {
    return request('/task/us/rv/query/users', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//获取svg列表
export async function getSvgList(params) {
    return request('/task/us/rv/check/search/markDatas', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//获取IAA
export async function getIaaNum(params) {
    return request('/task/getIAA', {
        params: {
            ...params
        }
    })
}

//完成校验
export async function finishCheck(params) {
    return request('/task/us/rv/check/submit', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//生成实例
export async function createInstance(params) {
    return request(`knowledge/createInstance`, {
        params: { ...params }
    })
}

//生成实例
export async function getModalList(params, taskId) {
    return request(`/task/us/rv/check/search/position/markList?taskId=${taskId}`, {
        body: { ...params },
        isForm: true,
        method: "POST"
    })
}
















