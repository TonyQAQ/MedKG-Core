import React from 'react'
import { Select, Input, Button } from 'antd';
import { MDS_BUTTON_GREEN, MDS_BUTTON_ORANGE, MDS_BUTTON_WARM } from './status'
import './index.less'

function SelectWrap(props) {
    return (
        <div className='mds-actionWrap' id='sel-wrap'>
            <label>
                {props.label}：
            </label>
            <Select
                size={props.size}
                style={props.style ? props.style : { width: '100px' }}
                allowClear={props.allowClear}
                onChange={props.selChange}
                getPopupContainer={() => document.getElementById('sel-wrap')}
                disabled={props.disabled}
            >
                {
                    props.list.map(item => {
                        return (
                            <Select.Option key={item.code} value={item.code} record={item}>
                                {item.value}
                            </Select.Option>
                        )
                    })
                }
            </Select>
        </div >
    )
}

function InputWrap(props) {
    return (
        <div className='mds-actionWrap'>
            <label>
                {props.label}：
            </label>
            <Input
                onPressEnter={props.onPressEnter}
                size={props.size}
                style={props.style ? props.style : { width: '250px' }}
                onChange={props.inputChange}
            />
        </div>
    )
}

function MyButton(props) {
    return (
        <Button
            className={
                props.classType === 'green'
                    ?
                    MDS_BUTTON_GREEN
                    :
                    (props.classType === 'orange'
                        ?
                        MDS_BUTTON_ORANGE
                        :
                        props.classType === 'warm'
                            ?
                            MDS_BUTTON_WARM
                            :
                            null)
            }
            size={props.size ? props.size : 'middle'}
            type={props.type ? props.type : 'primary'}
            onClick={props.onClick}
            danger={props.danger ? true : false}
            disabled={props.disabled ? true : false}
            style={props.style}
            htmlType={props.htmlType}
        >
            {props.label}
        </Button>
    )
}

export { SelectWrap, InputWrap, MyButton }
