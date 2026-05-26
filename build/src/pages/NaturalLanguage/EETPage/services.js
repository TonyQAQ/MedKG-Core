import request from '@/util/request';

//标签类别列表
export async function getLabelTypeList(params) {
    return request('/task/us/ussm/exe/search/codes', {
        params: params,
    })
}

//获取树
export async function getTreeData(params) {
    return request('/nlp/common/search/tree', {
        body: params,
        method: "POST"
    })
}


//获取schmaList
export async function getSchmaList(params) {
    return request('/nlp/eet/search/schema', {
        body: params,
        method: "POST"
    })
}

//获取SVG列表
export async function getSvgList(params) {
    return request(`/nlp/eet/search/markDatas`, {
        body: { ...params },
        method: "POST"
    })
}

//更新实体标签
export async function updateLabel(params, taskId) {
    return request(`/nlp/eet/update/labelCategories?taskId=${taskId}`, {
        body: params,
        isForm: true,
        method: "POST"
    })
}

//保存svg信息
export async function saveSvgInfo(params, taskId, paths) {
    let result
    // if (paths.indexOf("\"") > -1) {
    //     newPath = newNodePath.replace(/\"/g, '%22')
    // }
    result = paths.replace(/\"/g, '%22')
    result = paths.replace(/\#/g, '%23')
    result = paths.replace(/\%/g, '%25')
    result = paths.replace(/\&/g, '%26')
    result = paths.replace(/\+/g, '%2B')
    return request(`/nlp/eet/save/markDatas?taskId=${taskId}&paths=${result}`, {
        body: params,
        isForm: true,
        method: "POST"
    })
}

//更新任务状态
export async function updateTaskStatu(params) {
    return request(`/task/us/ql/update/state`, {
        body: { ...params },
        method: "POST"
    })
}

// 生成任务实例
export async function createInstance(params) {
    return request(`knowledge/createInstance`, {
        params: { ...params }
    })
}




















