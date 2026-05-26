import request from '@/util/request';

//获取数据库配置列表
export async function getLeftList(params) {
    return request('/task/rdfs/search/tablsAndColumns', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//保存连线
export async function saveConnection(params) {
    return request('/task/rdfs/addOrUpdate/rel', {
        body: {
            ...params
        },
        isForm: true,
        method: "POST"
    })
}

//多条件关系查询
export async function getRightList(params) {
    return request('/task/rdfs/search/rel', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//删除关系
export async function deleteRDF(params) {
    return request('/task/rdfs/delete/rel', {
        body: {
            ...params
        },
        method: "POST"
    })
}
//任务完成提交确认接口
export async function confirmSubmitInfo(params) {
    return request('/task/rdfs/submitConfirm', {
        params: {
            ...params
        }
    })
}

//任务提交
export async function confirmSubmit(params) {
    return request('/task/rdfs/submit', {
        body: {
            ...params
        },
        method: "POST"
    })
}



