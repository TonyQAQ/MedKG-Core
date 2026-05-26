import React from 'react'
import { Result, Button } from 'antd'
import { createHashHistory } from 'history';

const history = createHashHistory();

export default function index() {
    return (
        <Result
            status="404"
            title="404"
            subTitle="抱歉，你访问的页面不存在。"
            extra={<Button type="primary" onClick={() => {
                history.push("/HKKS/HKKSIndex")
            }}>回到首页</Button>}
        />
    )
}
