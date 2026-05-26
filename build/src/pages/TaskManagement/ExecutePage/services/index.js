import request from '@/util/request';

//修改表别名接口
export async function changeTableName(params) {
    return request('/task/ready/setAlias', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//确认发布
export async function confirmPublish(params) {
    return request('/task/ready/publish', {
        body: {
            ...params
        },
        method: "POST"
    })
}


//查询指定字段下的数据
export async function getHoverList(params) {
    return request('/db/search/columnData', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//任务准备查询初始化表信息
export async function getReadyTableInfo(params) {
    return request(`/task/ready/search/tables`, {
        body: {
            ...params
        },
        method: "POST"
    })
}

//获取表字段预选统计数据
export async function getStatisticsInfo(params) {
    return request('/task/ready/search/statData', {
        params: {
            ...params
        }
    })
}


//任务准备查询表字段信息
export async function getTableInfo(params) {
    return request(`/task/ready/search/columns`, {
        body: {
            ...params
        },
        method: "POST"
    })
}

//发布确认信息
export async function publishConfirmInfo(params) {
    return request('/task/ready/publishConfirm', {
        body: {
            ...params
        },
        method: "POST"
    })
}
//选中或取消选中字段保存接口
export async function selectTableFields(params) {
    return request('/task/ready/select', {
        body: {
            ...params
        },
        method: "POST"
    })
}
