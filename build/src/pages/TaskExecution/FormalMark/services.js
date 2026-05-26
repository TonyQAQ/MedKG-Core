import request from '@/util/request';

//获取表列表
export async function getTableList(params) {
    return request('/task/us/fm/exe/search/tables', {
        body: {
            ...params
        },
        method: "POST"
    })
}


//获取表列表下的字段
export async function getFieldList(params) {
    return request('/task/us/fm/exe/search/columns', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//获取SVG列表
export async function getSvgList(params) {
    return request('/task/us/fm/exe/search/markDatas', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//获取DRWAER左侧列表
export async function getLiList(params) {
    return request('/task/us/fm/exe/search/guideLine/schema', {
        body: {
            ...params
        },
        method: "POST"
    })
}


//获取DRWAER右侧列表
export async function getDrawerSvgList(params) {
    return request('/task/us/fm/exe/search/guideLine/markDatas', {
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

//正式标注完成-提交确认信息
export async function getSubmitInfo(params) {
    return request('/task/us/fm/exe/submitConfirm', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//正式标注完成-提交
export async function confirmSubmit(params) {
    return request('/task/us/fm/exe/submit', {
        body: {
            ...params
        },
        method: "POST"
    })
}


//标注信息总体保存
export async function saveSvgInfo(params, taskId) {
    return request('/task/us/lt/exe/saveOrUpdate/markDatas?taskId=' + taskId, {
        body: [params],
        isForm: true,
        method: "POST"
    })
}