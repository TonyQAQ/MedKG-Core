import React from 'react'
import { Modal } from 'antd'
import './index.less'
import { InfoCircleTwoTone } from '@ant-design/icons'

export default function index(props) {
    return (
        <Modal
            title={(
                <div className='modal-header-wrap'>
                    <InfoCircleTwoTone />
                    <span>{props.title}</span>
                </div>
            )}
            visible={props.visible}
            // onOk={props.modalOk}
            onCancel={props.modalCancel}
            footer={props.footer}
            destroyOnClose
            maskClosable={false}
        >
            <div className='modal-body-wrap'>
                {props.children}
            </div>
        </Modal>
    )
}
