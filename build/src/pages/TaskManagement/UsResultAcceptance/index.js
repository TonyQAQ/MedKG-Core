import React, { useState, useEffect, Fragment } from 'react'
import LayoutHeader from '@/components/Header'
import { Input, Empty, Spin, Radio, Select, Pagination, Space, Tooltip, message, Button, Modal, Table } from 'antd'
import { MyButton } from '@/components/commonWrap';
import { Annotator } from 'poplar-annotation';
import { MenuFoldOutlined } from '@ant-design/icons';
import * as api from './services'
import MyModal from '@/components/Modal'
import $ from 'jquery'
import './index.less'

const { Option } = Select

export default function Index(props) {

    const [liList, setLiList] = useState([])
    const [svgList, setSvgList] = useState([])
    const [fieldList, setFieldList] = useState([])
    const [usersList, setUsersList] = useState([])
    const [dataSource, setDataSource] = useState([])
    const [labelCategories, setLabelCategories] = useState([])
    const [connectionCategories, setConnectionCategories] = useState([])
    const [currentSelectIndex, setCurrentSelectIndex] = useState(null)
    const [currentSelectFieldIndex, setCurrentSelectFieldIndex] = useState(null)
    const [spinning, setSpinning] = useState(true)
    const [svgSpinning, setSvgSpinning] = useState(false)
    const [modalVisible, setModalVisible] = useState(false)
    const [tableLoading, setTableLoading] = useState(true)
    const [finishVisible, setFinishVisible] = useState(false)
    const [radioValue, setRadioValue] = useState('01')
    const [liLevel, setLiLevel] = useState('level1')
    const [pageCurrent, setPageCurrent] = useState(1)
    const [pageSize, setPageSize] = useState(10)
    const [pageTotal, setPageTotal] = useState(0)
    const [taskId, setTaskId] = useState(null)
    const [keyWord, setKeyWord] = useState(null)
    const [tableMappingId, setTableMappingId] = useState(null)
    const [attrMappingId, setAttrMappingId] = useState(null)
    const [userCode, setUserCode] = useState(null)
    const [iaaNum, setIaaNum] = useState(0)
    const [iaaValue, setIaaValue] = useState(0)
    const [selectText, setSelectText] = useState('')
    const [selectId, setSelectId] = useState('')
    const [fromId, setFromId] = useState('')
    const [toId, setToId] = useState('')
    const [rowIndex, setRowIndex] = useState('')
    const [evalId, setEvalId] = useState('')
    const [fromLabel, setFromLabel] = useState('')
    const [endLabel, setEndLabel] = useState('')
    const [clickType, setClickType] = useState('')
    const [backUserIds, setBackUserIds] = useState([])
    const [columns, setColumns] = useState([])

    useEffect(() => {
        const { taskId } = props.match.params
        setTaskId(taskId)
    }, [])


    // 请求左侧列表level1-表
    useEffect(() => {
        if (taskId) {
            getTable()
            getUsers()
            getIaaNum()
        }
    }, [taskId])

    // 请求左侧列表level2-表字段
    useEffect(() => {
        if (tableMappingId) {
            getFieldList()
            setCurrentSelectFieldIndex(null)
        }
    }, [tableMappingId])

    //请求svg列表
    useEffect(() => {
        if (attrMappingId && userCode && radioValue && pageCurrent && pageSize) {
            getSvgList()
        }
    }, [attrMappingId, userCode, radioValue, pageCurrent, pageSize])

    // 获取弹窗表格
    useEffect(() => {
        if (modalVisible) {
            setTimeout(() => {
                getModalTableList()
            }, 500);
        }
    }, [modalVisible])

    // 获取弹窗表格数据
    const getModalTableList = () => {
        api.getModalList({
            attrMappingId,
            rowIndex,
            evalId,
            fromId,
            toId,
            // labelId: selectId
        }, taskId).then(res => {
            const { data, titleHead } = res
            setDataSource(data)
            setColumns(titleHead)
            setTableLoading(false)
        }).catch(err => { })
    }


    // 获取IAA
    const getIaaNum = () => {
        api.getIaaNum({
            taskId,
            type: '01'
        }).then(res => {
            setIaaNum(res.IAA)
        }).catch(err => { })
    }

    // 获取弹窗label表格信息
    const getModalLabelTable = () => {

    }

    // 获取弹窗联系表格信息
    const getModalConnectionTable = () => {

    }


    // 获取用户表
    const getUsers = () => {
        api.getUsers({
            taskId
        }).then(res => {
            const { data } = res
            setUsersList(data)
            setUserCode(data.length > 0 ? data[0].code : null)
        }).catch(err => {
        })
    }


    // 获取左侧列表level1-表
    const getTable = () => {
        api.getTable({
            taskId,
            keyWord
        }).then(res => {
            setLiList(res.data)
            setSpinning(false)
        }).catch(err => {
            setSpinning(false)
        })
    }

    // 获取左侧列表level2-表字段
    const getFieldList = () => {
        api.getFieldList({
            taskId,
            keyWord,
            tableMappingId
        }).then(res => {
            const { data } = res
            setFieldList(data)
            setSpinning(false)
        }).catch(err => {
            setSpinning(false)
        })
    }

    // 获取svg列表
    const getSvgList = () => {
        api.getSvgList({
            taskId,
            attrMappingId,
            isCommon: radioValue,
            pageSize,
            pageCount: pageCurrent,
            exeUserId: userCode
        }).then(res => {
            const { data, totalCount, labelCategories, connectionCategories } = res
            const temp1 = JSON.parse(JSON.stringify(labelCategories))
            const temp2 = JSON.parse(JSON.stringify(connectionCategories))
            setLabelCategories(temp1)
            setConnectionCategories(temp2)
            setSvgList(data)
            setPageTotal(totalCount)
            setSvgSpinning(false)
        }).catch(err => {
            setSvgSpinning(false)
        })
    }

    //标注类型切换
    const radioChange = ({ target: { value } }) => {
        if (attrMappingId) {
            setSvgSpinning(true)
        }
        setRadioValue(value)
        clearSvg(1)
    }

    // 翻页
    const pageChange = (page, pageSize) => {
        setSvgSpinning(true)
        setPageSize(pageSize)
        clearSvg(page)
    }

    // 左侧列表输入框事件
    const inputChange = ({ target: { value } }) => {
        setKeyWord(value)
    }

    // 渲染SVG
    const renderMainSvg = (item, index) => {
        const len = $("#svgMainAllContainer").children("#svgMainContainer" + index).length
        if (len === 0) {
            $('#svgMainAllContainer').append("<div class='svgMainContainer' id='svgMainContainer" + index + "'></div>")
        }
        let svgDom = document.getElementById('svgMainContainer' + index)
        const tempArry = JSON.parse(JSON.stringify(item.annotation.labelCategories))
        const tempArry2 = JSON.parse(JSON.stringify(item.annotation.connectionCategories))
        if (svgDom && $('#svgMainContainer' + index).children().length === 0) {
            let annotator = new Annotator({
                content: item.annotation.content,
                labelCategories: tempArry,
                labels: item.annotation.labels,
                connectionCategories: tempArry2,
                connections: item.annotation.connections,
            }, document.getElementById('svgMainContainer' + index), {
                labelWidthCalcMethod: 'max',
                unconnectedLineStyle: 'none'
            })
            if (radioValue === '01') {
                annotator.on('labelClicked', (id, e) => {
                    console.log(annotator)
                    const labels = annotator.store.json.labels
                    const len = labels.length
                    const labelCategories = annotator.store.json.labelCategories
                    const len2 = labelCategories.length
                    let labelCategoryId
                    let selectId
                    let selectText
                    for (let idx = 0; idx < len; idx++) {
                        if (labels[idx].id === id) {
                            labelCategoryId = labels[idx].categoryId
                            setFromId(labels[idx].startIndex)
                            setToId(labels[idx].endIndex)
                            selectText = annotator.store.json.content.substring(labels[idx].startIndex, labels[idx].endIndex)
                            break;
                        }
                    }
                    for (let idx2 = 0; idx2 < len2; idx2++) {
                        if (labelCategories[idx2].id === labelCategoryId) {
                            selectId = labelCategories[idx2].id

                            break;
                        }

                    }
                    setAttrMappingId(item.attrMappingId)
                    setRowIndex(item.rowIndex)
                    setEvalId(item.evalId)
                    setSelectId(selectId)
                    setSelectText(selectText)
                    setModalVisible(true)
                    setClickType('labelClicked')
                })
                annotator.on('connectionClicked', (id, e) => {
                    const connections = annotator.store.json.connections
                    const len = connections.length
                    const labels = annotator.store.json.labels
                    const len2 = labels.length
                    let fromId, toId, fromLabel, endLabel, startIndex, endIndex
                    for (let idx = 0; idx < len; idx++) {
                        if (connections[idx].id === id) {
                            fromId = connections[idx].fromId
                            toId = connections[idx].toId
                            setSelectId(connections[idx].categoryId)
                            break;
                        }
                    }
                    for (let idx2 = 0; idx2 < len2; idx2++) {
                        if (labels[idx2].id === fromId) {
                            startIndex = labels[idx2].startIndex + "--AA--" + labels[idx2].endIndex
                            fromLabel = annotator.store.json.content.substring(labels[idx2].startIndex, labels[idx2].endIndex)
                        } else if (labels[idx2].id === toId) {
                            endIndex = labels[idx2].startIndex + "--AA--" + labels[idx2].endIndex
                            endLabel = annotator.store.json.content.substring(labels[idx2].startIndex, labels[idx2].endIndex)
                        }
                    }
                    if (fromId === toId) {
                        endLabel = fromLabel
                        endIndex = startIndex
                    }
                    setFromId(startIndex)
                    setToId(endIndex)
                    setAttrMappingId(item.attrMappingId)
                    setRowIndex(item.rowIndex)
                    setEvalId(item.evalId)
                    setFromLabel(fromLabel)
                    setEndLabel(endLabel)
                    setClickType('connectionClicked')
                    setModalVisible(true)
                })
            }
        }
    }

    // 输入框搜索
    const onSearch = () => {
        if (liLevel === 'level1') {
            getTable()
        } else if (liLevel === 'level2') {
            getFieldList()
        }
    }

    // 弹窗取消
    const modalCancel = (params) => {
        setModalVisible(false)
        setDataSource([])
        setTableLoading(true)
    }

    function liClick(item, index, type) {
        if (type === 'level1') {
            const { tableMappingId } = item
            setTableMappingId(tableMappingId)
            setLiLevel('level2')
            setKeyWord(null)
            setCurrentSelectIndex(index)
        } else if (type === 'level2') {
            setLiLevel('level1')
            setKeyWord(null)
        } else if (type === 'field') {
            if (currentSelectFieldIndex !== index) {
                const { attrMappingId } = item
                clearSvg(1)
                setSvgSpinning(true)
                setCurrentSelectFieldIndex(index)
                setAttrMappingId(attrMappingId)
            }
        }
    }

    function btnClick(type) {
        if (type === 'finish') {
            setFinishVisible(true)
        } else if (type === 'cancel') {
            window.close()
        }
    }

    // 弹窗确认
    function modalOk(type) {
        if (type === 'confirm') {
            let submitUserIds = []
            usersList.filter(item => {
                (!(backUserIds.includes(item.code))) && submitUserIds.push(item.code);
            });
            api.finishCheck({
                submitUserIds: submitUserIds.toString(),
                backUserIds: backUserIds.toString(),
                taskId
            }).then(res => {
                if (backUserIds.length === 0) {
                    setFinishVisible(false)
                    message.loading('任务实例生成中...', 0)
                    api.createInstance({
                        taskId
                    }).then(res => {
                        message.destroy()
                        message.loading('任务实例生成成功，窗口即将关闭！')
                        window.opener.location.reload();
                        setTimeout(() => {
                            window.close()
                        }, 500);
                    }).catch(err => { })
                } else {
                    message.success('操作成功，窗口即将关闭！')
                    window.opener.location.reload()
                    setTimeout(() => {
                        window.close()
                    }, 500);
                }
            }).catch(err => {

            })
        }
    }

    // 用户选择事件
    function selChange(value, type) {
        if (type === 'user') {
            setSvgSpinning(true)
            setUserCode(value)
            clearSvg(1)
        } else {
            setBackUserIds(value)
        }

    }


    // 清除svg
    function clearSvg(page) {
        $('.svgMainContainer').remove()
        setSvgList([])
        setPageCurrent(page)
    }

    return (
        <div className='mds-all-layout-wrap' >
            <LayoutHeader />
            <div className='mds-executePage-wrap'>
                <div className='mds-executePage-contentWrap' style={{ display: 'flex', flex: '1', height: '100%', overflowY: 'auto' }}>
                    <Spin spinning={spinning} tip='加载中...' style={{ zIndex: '1' }}>
                        <div className='mds-executePage-leftWrap' id='tooltipWrap'>
                            <div className='mds-executePage-conditionWrapL'>
                                <Input.Search placeholder={liLevel === 'level1' ? '输入表名' : '输入表字段名'} value={keyWord} enterButton onSearch={onSearch} onChange={inputChange} />
                            </div>
                            <ul className={'mds-executePage-ulWrap'} style={liLevel === 'level1' ? {
                                transform: 'translateX(0%)'
                            } : { transform: 'translateX(-101%)' }}>
                                {
                                    liList.length > 0
                                        ?
                                        liList.map((item, index) => {
                                            return (
                                                <Tooltip
                                                    key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
                                                    getPopupContainer={() => document.getElementById('tooltipWrap')}
                                                    overlayClassName='liToolTip'
                                                    placement='right'
                                                    title={
                                                        <div dangerouslySetInnerHTML={{ __html: item.levitationTips }}>
                                                        </div>
                                                    }
                                                >
                                                    <li
                                                        onClick={() => liClick(item, index, 'level1')}
                                                        className={
                                                            currentSelectIndex === index
                                                                ?
                                                                'mds-executePage-liWrap mds-executePage-liCurrentSelect'
                                                                :
                                                                (
                                                                    item.isMark === 'no'
                                                                        ?
                                                                        'mds-executePage-liWrap mds-executePage-liNoSelect'
                                                                        :
                                                                        'mds-executePage-liWrap mds-executePage-liSelect'
                                                                )

                                                        }
                                                    >
                                                        <span className='mds-executePage-liName'>{item.tableName}</span>
                                                    </li>
                                                </Tooltip>
                                            )
                                        })
                                        :
                                        <Empty style={{ marginTop: '35px' }} />
                                }
                            </ul>
                            <div className={'mds-executePage-ulWrap'} style={liLevel === 'level2' ? {
                                transform: 'translateX(0%)'
                            } : { transform: 'translateX(-101%)' }}>
                                <ul>
                                    {
                                        fieldList.length > 0
                                            ?
                                            fieldList.map((item, index) => {
                                                return (
                                                    <li
                                                        key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
                                                        onClick={() => liClick(item, index, 'field')}
                                                        className={
                                                            currentSelectFieldIndex === index
                                                                ?
                                                                'mds-executePage-liWrap mds-executePage-liCurrentSelect'
                                                                :
                                                                (
                                                                    item.isMark === 'no'
                                                                        ?
                                                                        'mds-executePage-liWrap mds-executePage-liNoSelect'
                                                                        :
                                                                        'mds-executePage-liWrap mds-executePage-liSelect'
                                                                )

                                                        }
                                                    >
                                                        <span className='mds-executePage-liName'>{item.attrName}</span>
                                                    </li>
                                                )
                                            })
                                            :
                                            <Empty style={{ marginTop: '35px' }} />
                                    }
                                </ul>
                                {React.createElement(MenuFoldOutlined, {
                                    className: 'mds-usResultAcceptance-trigger',
                                    style: {
                                        display: liLevel === 'level1' ? 'none' : 'block',
                                    },
                                    onClick: () => liClick('', '', 'level2'),
                                })}
                            </div>
                        </div>
                    </Spin>
                    <div className='mds-usResultAcceptance-rightWrap'>
                        <div className='mds-usResultAcceptance-headWrap'>
                            <Radio.Group value={radioValue} buttonStyle="solid" onChange={radioChange}>
                                <Radio.Button value="01">共同标注</Radio.Button>
                                <Radio.Button value="02">非共同标注</Radio.Button>
                            </Radio.Group>
                            {
                                radioValue === '01'
                                    ?
                                    <div className='mds-usResultAcceptance-iaa'>
                                        本次训练的IAA值为：{iaaNum}
                                    </div>
                                    :
                                    <Select value={userCode} style={{ width: "200px" }} onChange={(value) => selChange(value, 'user')}>
                                        {
                                            usersList.map(item => {
                                                return (
                                                    <Option value={item.code}>
                                                        {item.value}
                                                    </Option>
                                                )
                                            })
                                        }
                                    </Select>
                            }

                        </div>
                        <div className='mds-usResultAcceptance-middleWrap'>
                            <Spin style={{ height: '100%' }} spinning={svgSpinning}>
                                <div id='svgMainAllContainer'>
                                    {
                                        svgList.length > 0
                                            ?
                                            svgList.map((item, index) => {
                                                return (
                                                    renderMainSvg(item, index)
                                                )
                                            })
                                            :
                                            <Empty style={{ marginTop: '35px' }} />
                                    }
                                </div>
                            </Spin>
                        </div>
                        <Pagination
                            className='mds-usResultAcceptance-svgPageNation'
                            current={pageCurrent}
                            pageSize={pageSize}
                            onChange={pageChange}
                            total={pageTotal}
                        />
                        <div className='mds-usResultAcceptance-btns'>
                            <Space size='large'>
                                <MyButton
                                    label='完成'
                                    onClick={() => btnClick('finish')}
                                />
                                <MyButton
                                    label='取消'
                                    onClick={() => btnClick('cancel')}
                                    classType='warm'
                                />
                            </Space>
                        </div>
                    </div>
                </div>
            </div>
            <MyModal
                visible={finishVisible}
                modalCancel={() => {
                    setFinishVisible(false)
                }}
                title={'提示'}
                footer={
                    <Fragment>
                        <Button type={'primary'} onClick={() => modalOk('confirm')}>确定</Button>
                        <MyButton label='取消' classType='warm' onClick={() => {
                            setFinishVisible(false)
                        }} />
                    </Fragment>
                }
            >
                <Fragment>
                    <p>如果有需要重新标注的人员，请选择：</p>
                    <Select mode='multiple' style={{ width: '100%' }} onChange={(value) => selChange(value, 'finish')}>
                        {
                            usersList.map(item => {
                                return (
                                    <Option value={item.code}>
                                        {item.value}
                                    </Option>
                                )
                            })
                        }
                    </Select>
                </Fragment>
            </MyModal>
            <Modal
                title={
                    clickType === 'connectionClicked'
                        ?
                        <div style={{ textAlign: 'center' }}> <Space><span>From：{fromLabel}</span><span>To：{endLabel}</span></Space></div>
                        :
                        <div style={{ textAlign: 'center' }}>{selectText}</div>
                }
                visible={modalVisible}
                onCancel={modalCancel}
                width="60%"
                destroyOnClose
                footer={
                    <Fragment>
                        <Button type={'primary'} onClick={() => modalOk('checkTable')}>确定</Button>
                        <MyButton label='取消' classType='warm' onClick={modalCancel} />
                    </Fragment>
                }
            >
                <div className='mds-UsMarkTrainingCheck-modalTable'>
                    <Table
                        loading={tableLoading}
                        dataSource={dataSource}
                        columns={columns}
                        pagination={false}
                    />
                </div>
            </Modal>
        </div>
    )
}
