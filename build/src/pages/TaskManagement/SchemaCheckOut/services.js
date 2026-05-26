import request from '@/util/request';

//schema查询
export async function getSchemaList(params) {
    return request('/task/us/sc/check/search/schema', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//试题标签类别列表
export async function getLabelTypeList(params) {
    return request('/task/us/ussm/exe/search/codes', {
        params: {
            ...params
        }
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

//Schema回退
export async function goBackSchema(params) {
    return request('/task/us/sc/check/back', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//添加Schema标签
export async function addOrUpdateSchema(params) {
    return request('/task/us/sc/check/saveOrUpdate/schema', {
        body: {
            ...params
        },
        isForm: true,
        method: "POST"
    })
}

//删除Schema标签
export async function deleteSchema(params) {
    return request('/task/us/sc/check/delete/schema', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//更新例子状态（正面，反面，删除）
export async function updateSchemaTag(params) {
    return request('/task/us/sc/check/update/tag', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//获取用户
export async function getUsers(params) {
    return request('/task/us/sc/query/users', {
        params: {
            ...params
        }
    })
}

//Schema提交【确认信息 】接口
export async function getConfirmInfo(params) {
    return request('/task/us/sc/check/publishConfirm', {
        body: {
            ...params
        },
        method: "POST"
    })
}













