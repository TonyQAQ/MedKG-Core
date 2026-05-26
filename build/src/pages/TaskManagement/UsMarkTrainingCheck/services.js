import request from '@/util/request';

//获取主要svg
export async function getMainSvgList(params) {
    return request('/task/us/mtc/check/search/markDatas', {
        body: {
            ...params
        },
        method: "POST"
    })
}


//重新标注
export async function reMark(params) {
    return request('/task/us/mtc/check/back', {
        body: { ...params },
        method: "POST"
    })
}

//获取提交弹框信息
export async function getModalInfo(params) {
    return request('/task/us/mtc/check/publishConfirm', {
        body: { ...params },
        method: "POST"
    })
}

//点击校验弹出的信息列表查询
export async function getModalList(params) {
    return request('/task/us/mtc/check/search/position/markList', {
        body: { ...params },
        isForm: true,
        method: "POST"
    })
}

//校验标签
export async function updataTag(params) {
    return request('/task/us/mtc/check/update/tag', {
        body: { ...params },
        method: "POST"
    })
}

//确认完成
export async function confirmSubmit(params) {
    return request('/task/us/mtc/check/publish', {
        body: { ...params },
        method: "POST"
    })
}

//获取IAA
export async function getIaaNum(params) {
    return request('/task/getIAA', {
        params: { ...params }
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





