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

//分页查询字段标注数据信息
export async function getLabelList(params) {
    return request('/task/us/ussm/exe/search/markDatas', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//保存
export async function saveLabelList(params) {
    return request('/task/us/ussm/exe/saveOrUpdate/labels', {
        body: params,
        isForm: true,
        method: "POST"
    })
}

//多条件标注表信息查询
export async function iniLeftTable(params) {
    return request('/task/us/ussm/exe/search/tables', {
        body: params,
        method: "POST"
    })
}

//多条件标注表信息查询
export async function iniLeftField(params) {
    return request('/task/us/ussm/exe/search/columns', {
        body: params,
        method: "POST"
    })
}

//多条件获取个人标签表
export async function iniRightLabelList(params) {
    return request('/task/us/ussm/exe/search/labels', {
        body: params,
        method: "POST"
    })
}

//多条件获取个人标签表
export async function iniRightSelList(params) {
    return request('/task/us/ussm/exe/search/codes', {
        params: params,
    })
}

//添加和修改个人标签信息
export async function addOrUpdataLabel(params) {
    return request('/task/us/ussm/exe/saveOrUpdate/label', {
        body: params,
        isForm: true,
        method: "POST"
    })
}

//逐个标注记录保存接口
export async function saveMakeLabel(params) {
    return request('/task/us/ussm/exe/saveOrUpdate/markSampleInfo', {
        body: {...params},
        isForm: true,
        method: "POST"
    })
}

//标注信息保存接口
export async function saveSvgInfo(params, taskId) {
    return request('/task/us/ussm/exe/saveOrUpdate/markDatas?taskId=' + taskId, {
        body: [params],
        isForm: true,
        method: "POST"
    })
}


//逐个标注记录删除接口
export async function delSvgInfo(params) {
    return request('/task/us/ussm/exe/delete/markSampleInfo', {
        body: params,
        isForm: true,
        method: "POST"
    })
}

//获取提交信息
export async function publishConfirmInfo(params) {
    return request('/task/us/ussm/exe/submitConfirm', {
        body: params,
        method: "POST"
    })
}

//提交
export async function confirmPublish(params) {
    return request('/task/ssm/exe/submit', {
        body: params,
        method: "POST"
    })
}







