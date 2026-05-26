import request from '@/util/request';

//标签类别列表
export async function getLabelTypeList(params) {
    return request('/task/us/ussm/exe/search/codes', {
        params: {
            ...params
        }
    })
}

//标签类别列表
export async function getLiList(params) {
    return request('/task/us/sc/check/search/guideLine/schema', {
        body: {
            ...params
        },
        method: "POST"
    })
}


//Schema的JSON数据查询
export async function getSchemaJsonList(params) {
    return request('/task/us/sc/check/search/markDatas', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//Schema的JSON数据查询
export async function finish(params) {
    return request('/task/us/sc/check/publish', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//最大训练量
export async function getMaxCount(params) {
    return request('/task/us/sc/search/maxTrainCount', {
        body: {
            ...params
        },
        method: "POST"
    })
}















