import request from '@/util/request';

//获取表格信息
export async function getTableList(params) {
    return request('/map/search/mapList', {
        body: {
            ...params
        },
        method: "POST"
    })
}

//获取下拉框信息
export async function getSelList(params) {
    return request('/map/codeTable/state', {
        params: {
            ...params
        }
    })
}

//生成图谱
export async function createAtlas(params) {
    return request('/knowledge/create', {
        params: {
            ...params
        }
    })
}
