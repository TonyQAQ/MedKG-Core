import request from '@/util/request';

//获取数据库配置列表
export async function preClick(params) {
    return request('/task/us/ussm/exe/back', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//guidelLine预览例子查询
export async function getDrawerSchemaList(params) {
    return request('/task/us/sc/check/search/markDatas', {
        body: {
            ...params
        },
        method: "POST"
    })
}

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

//获取主要svg
export async function getMainSvgList(params) {
    return request('/task/us/lt/exe/search/markDatas', {
        body: {
            ...params
        },
        method: "POST"
    })
}


//逐个标注记录保存接口
export async function saveMakeLabel(params) {
    return request('/task/us/lt/exe/saveOrUpdate/markSampleInfo', {
        body: { ...params },
        isForm: true,
        method: "POST"
    })
}

//删除svg
export async function delSvgInfo(params) {
    return request('/task/us/lt/exe/delete/markSampleInfo', {
        body: params,
        isForm: true,
        method: "POST"
    })
}

//标注信息保存接口
export async function saveSvgInfo(params, taskId) {
    return request('/task/us/lt/exe/saveOrUpdate/markDatas?taskId=' + taskId, {
        body: [params],
        isForm: true,
        method: "POST"
    })
}

//获取提交弹框信息
export async function getModalInfo(params) {
    return request('/task/us/lt/exe/submitConfirm', {
        body: { ...params },
        method: "POST"
    })
}

//任务提交
export async function confirmSubmit(params) {
    return request('/task/us/lt/exe/submit', {
        body: { ...params },
        method: "POST"
    })
}



