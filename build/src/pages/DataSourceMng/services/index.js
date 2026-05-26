import request from '@/util/request';

//获取数据库配置列表
export async function getDataSourceList(params) {
    return request('/db/search/database', {
        params: {
            ...params
        }
    })
}

//测试连接
export async function testLink(params) {
    return request('/db/test/connection', {
        body: {
            ...params
        },
        isForm: true,
        method: 'POST'
    })
}

//保存连接
export async function saveOrEditLink(params) {
    return request('/db/saveOrUpdate/database', {
        body: {
            ...params
        },
        isForm: true,
        method: 'POST'
    })
}

//获取数据库类型信息
export async function getSourceTypeInfo(params) {
    return request('/db/search/dbType', {
        params: {
            ...params
        }
    })
}

//测试数据源是否能被删除
export async function getSourceJudgeDeleteInfo(params) {
    return request('/db/delete/state', {
        params: {
            ...params
        }
    })
}

//确认删除
export async function dataSourceMngDelete(params) {
    return request('/db/delete/database', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//测试数据源是否能被编辑
export async function getSourceJudgeEditInfo(params) {
    return request('/db/update/state', {
        params: {
            ...params
        }
    })
}







