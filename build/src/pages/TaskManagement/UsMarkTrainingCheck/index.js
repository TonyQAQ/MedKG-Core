import React, { useEffect, useState, Fragment } from 'react'
import LayoutHeader from '@/components/Header'
import './index.less'
import '../../TaskManagement/ConfirmGuideLinePage/index.less'
import * as api from './services'
import { Select, Button, Spin, Pagination, Empty, message, Space, Tag, InputNumber, Modal, Table } from 'antd'
import { Annotator } from 'poplar-annotation';
import { MyButton } from '@/components/commonWrap';
import MyModal from '@/components/Modal'
import { InfoCircleTwoTone } from '@ant-design/icons';
import $ from 'jquery'

const { Option } = Select;
const { CheckableTag } = Tag;

export default function Index(props) {

    const [pageCurrent, setPageCurrent] = useState(1)
    const [pageSize, setPageSize] = useState(10)
    const [pageTotal, setPageTotal] = useState(0)
    const [svgList, setSvgList] = useState([])
    const [svgSpinning, setSvgSpinning] = useState(true)
    const [taskId, setTaskId] = useState(null)
    const [isCheck, setIsCheck] = useState('all')
    const [modalVisible, setModalVisible] = useState(false)
    const [modalVisible2, setModalVisible2] = useState(false)
    const [modalVisible3, setModalVisible3] = useState(false)
    const [modalContent, setModalContent] = useState('')
    const [maxCount, setMaxCount] = useState(0)
    const [iaaValue, setIaaValue] = useState(0)
    const [selectText, setSelectText] = useState('')
    const [selectId, setSelectId] = useState('')
    const [fromId, setFromId] = useState('')
    const [toId, setToId] = useState('')
    const [attrMappingId, setAttrMappingId] = useState('')
    const [rowIndex, setRowIndex] = useState('')
    const [evalId, setEvalId] = useState('')
    const [trainCount, setTrainCount] = useState(100)
    const [dataSource, setDataSource] = useState([])
    const [columns, setColumns] = useState([])
    const [checkList, setCheckList] = useState(null)
    const [tableLoading, setTableLoading] = useState(true)
    const [fromLabel, setFromLabel] = useState('')
    const [endLabel, setEndLabel] = useState('')
    const [clickType, setClickType] = useState('')

    useEffect(() => {
        const { taskId } = props.match.params
        setTaskId(taskId)
    }, [])

    useEffect(() => {
        if (taskId && pageCurrent && isCheck && pageSize) {
            getMainSvgList()
        }
    }, [taskId, pageCurrent, isCheck, pageSize])

    useEffect(() => {
        if (modalVisible3) {
            setTimeout(() => {
                getModalTableList()
            }, 500);
        }
    }, [modalVisible3])

    useEffect(() => {
        if (taskId) {
            getIaaNum()
            api.getMaxCount({ taskId }).then(res => {
                setMaxCount(res.maxTrainCount)
            })
        }
    }, [taskId])


    // 获取IAA
    const getIaaNum = () => {
        api.getIaaNum({
            taskId,
            type: '00'
        }).then(res => {
            setIaaValue(res.IAA)
        }).catch(err => { })
    }

    // 获取主要svg
    const getMainSvgList = () => {
        api.getMainSvgList({
            pageSize,
            pageCount: pageCurrent,
            isCheck,
            taskId
        }).then(res => {
            setSvgSpinning(false)
            setSvgList(res.data)

            setPageTotal(res.totalCount)
        }).catch(err => {
            setSvgSpinning(false)
        })
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
        // const tempArry = JSON.parse(JSON.stringify(this.state.labelCategories))
        // const tempArry2 = JSON.parse(JSON.stringify(this.state.connectionCategories))
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
                        // selectText = labelCategories[idx2].text
                        break;
                    }

                }
                setAttrMappingId(item.attrMappingId)
                setRowIndex(item.rowIndex)
                setEvalId(item.evalId)
                setSelectId(selectId)
                setSelectText(selectText)
                setModalVisible3(true)
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
                setModalVisible3(true)
            })
        }
    }

    const getModalTableList = () => {
        api.getModalList({
            attrMappingId,
            rowIndex,
            evalId,
            fromId,
            toId,
            // labelId: selectId
        }).then(res => {
            const { data } = res
            let newCheckList = {}
            res.data.map(item => {
                newCheckList[item.sampleIds] = item.tag
            })
            setDataSource(data)
            setCheckList(newCheckList)
            setTableLoading(false)
        }).catch(err => { })
    }


    // 数字输入框事件
    const numberChange = (value) => {
        setTrainCount(value)
    }

    // 弹窗确认
    function modalOk(type) {
        if (type === 'reMark') {
            api.reMark({
                taskId,
                trainCount
            }).then(res => {
                message.success('操作成功，窗口即将关闭！')
                window.opener.location.reload()
                setTimeout(() => {
                    window.close()
                }, 500);
            }).catch(err => { })
        } else if (type === 'checkTable') {
            setModalVisible3(false)
            setTableLoading(true)
            setDataSource([])
        } else if (type === 'finish') {
            api.confirmSubmit({
                taskId,
                trainCount
            }).then(res => {
                message.success('操作成功，窗口即将关闭！')
                window.opener.location.reload()
                setTimeout(() => {
                    window.close()
                }, 500);
            }).catch(err => { })
        }
    }

    // checkTag事件
    function tagChange(record, checked, type) {
        let newList = JSON.parse(JSON.stringify(checkList))
        const { sampleIds } = record
        if (type === '01' && checked || type === '02' && checked) {
            newList[sampleIds] = type
        } else {
            newList[sampleIds] = '00'
        }
        api.updataTag({
            sampleIds,
            tag: newList[sampleIds]
        }).then(res => {
            setCheckList(newList)
        }).catch(err => { })
    }

    // 弹窗取消
    function modalCancel(params) {
        setTrainCount(100)
        setModalVisible(false)
        setModalVisible2(false)
        setModalVisible3(false)
        setTableLoading(true)
        setDataSource([])
    }



    // 翻页事件
    function pageChange(page, size) {
        $('.svgMainContainer').remove()
        setSvgList([])
        setSvgSpinning(true)
        setPageCurrent(page)
        setPageSize(size)
    }

    // 按钮事件
    function btnClick(type, record) {
        if (type === 'save') {
            message.success('保存成功！')
        } else if (type === 'finish') {
            api.getModalInfo({
                taskId
            }).then(res => {
                const { data: { publishComfirm, publishState } } = res
                setModalVisible(true)
                setModalContent(publishComfirm)
            }).catch(err => { })
        } else if (type === 'cancel') {
            window.close()
        } else if (type === 'reMark') {
            setModalVisible2(true)
        } else if (type === 'delete') {
            const { sampleIds } = record
            Modal.confirm({
                title: "确认删除吗？",
                onOk() {
                    setDataSource([])
                    api.updataTag({
                        sampleIds,
                        tag: '03'
                    }).then(res => {
                        message.success('删除成功！')
                        getModalTableList()
                    }).catch(err => { })
                },
                cancelButtonProps: {
                    className: 'mds-btn-warm',
                    style: {
                        marginLeft: '8px'
                    }
                }
            })
        }
    }

    function selChange(value) {
        $('.svgMainContainer').remove()
        setSvgList([])
        setSvgSpinning(true)
        setPageCurrent(1)
        setIsCheck(value)
    }

    return (
        <div className='mds-all-layout-wrap' >
            <LayoutHeader />
            <div className='mds-executePage-wrap'>
                <div className='mds-executePage-contentWrap' style={{ display: 'flex', flex: '1', height: '100%', overflowY: 'auto' }}>
                    <div className='mds-usMarkingTrainingCheck-bodyWrap'>
                        <div className='mds-usMarkingTrainingCheck-headWrap'>
                            <Select onChange={(value) => selChange(value, 'isCheck')} value={isCheck} style={{ width: '200px' }}>
                                <Option value='all'>全部</Option>
                                <Option value='01'>已验收</Option>
                                <Option value='02'>未验收</Option>
                            </Select>
                            <Button type='primary' onClick={() => btnClick('lookGuideLine')}>本次训练的IAA值为：{iaaValue}</Button>
                        </div>
                        <div className='mds-usMarkingTrainingCheck-middleWrap'>
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
                            className='mds-executePage-svgPageNation'
                            current={pageCurrent}
                            pageSize={pageSize}
                            onChange={(page, pageSize) => pageChange(page, pageSize)}
                            total={pageTotal}
                        />
                        <div className='mds-usMarkingTrainingCheck-btns'>
                            <Space size='large'>
                                <MyButton
                                    label='保存'
                                    onClick={() => btnClick('save')}
                                />
                                <MyButton
                                    label='重新标注'
                                    danger
                                    onClick={() => btnClick('reMark')}
                                />
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
            <Modal
                title="提示"
                visible={modalVisible}
                onCancel={modalCancel}
                destroyOnClose
                footer={
                    <Fragment>
                        <Button type={'primary'} onClick={() => modalOk('finish')}>确定</Button>
                        <MyButton label='取消' classType='warm' onClick={modalCancel} />
                    </Fragment>
                }
            >
                <Space align='center'>
                    <InfoCircleTwoTone style={{ fontSize: '20px', width: '100%' }} />
                    <InputNumber value={trainCount} min={1} max={maxCount} onChange={numberChange} />
                    <label>最大值共同标注量：{maxCount}</label>
                </Space>
                <Space align='center' style={{ marginTop: '20px', width: '100%' }}>
                    <InfoCircleTwoTone style={{ fontSize: '20px' }} />
                    <label>{modalContent}</label>
                </Space>
            </Modal>
            <Modal
                title="提示"
                visible={modalVisible2}
                onCancel={modalCancel}
                destroyOnClose
                footer={
                    <Fragment>
                        <Button type={'primary'} onClick={() => modalOk('reMark')}>确定</Button>
                        <MyButton label='取消' classType='warm' onClick={modalCancel} />
                    </Fragment>
                }
            >
                <Space align='center' style={{ width: '100%' }}>
                    <InfoCircleTwoTone style={{ fontSize: '20px' }} />
                    <InputNumber value={trainCount} min={1} max={maxCount} onChange={numberChange} />
                    <label>最大训练量：{maxCount}</label>
                </Space>
                <Space align='center' style={{ marginTop: '20px', width: '100%' }}>
                    <InfoCircleTwoTone style={{ fontSize: '20px' }} />
                    <div>确认进行重新训练吗？</div>
                </Space>
            </Modal>
            <Modal
                title={
                    clickType === 'connectionClicked'
                        ?
                        <div style={{ textAlign: 'center' }}> <Space><span>From：{fromLabel}</span><span>To：{endLabel}</span></Space></div>
                        :
                        <div style={{ textAlign: 'center' }}>{selectText}</div>
                }
                visible={modalVisible3}
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
                    {/* <div style={{ textAlign: 'right', marginBottom: '10px' }}>
                        <Button type='primary'>新增</Button>
                    </div> */}
                    <Table
                        loading={tableLoading}
                        dataSource={dataSource}
                        pagination={false}
                    >
                        <Table.Column
                            title={clickType === 'connectionClicked' ? '关系标签' : '实体标签'}
                            dataIndex='schemaName'
                            key='schemaName'
                            ellipsis='true'
                            render={(text, item, index) => {
                                return (
                                    <div>{text}</div>
                                )
                            }}
                        />
                        <Table.Column
                            title='所属用户'
                            dataIndex='users'
                            key='users'
                            ellipsis='true'
                            render={(text, item, index) => {
                                return (
                                    <div>{text}</div>
                                )
                            }}
                        />
                        <Table.Column
                            title='正面'
                            dataIndex='tag01'
                            key='tag01'
                            width='100px'
                            fixed='right'
                            render={(obj, record, index) => {
                                return (
                                    <CheckableTag
                                        className='checkIcon'
                                        checked={checkList ? (checkList[record.sampleIds] === '00' || checkList[record.sampleIds] === '02' ? false : true) : false}
                                        onChange={checked => tagChange(record, checked, '01')}
                                    >
                                    </CheckableTag>
                                )
                            }}
                        />
                        <Table.Column
                            title='负面'
                            dataIndex='tag02'
                            key='tag02'
                            width='100px'
                            fixed='right'
                            render={(obj, record, index) => {
                                return (
                                    <CheckableTag
                                        className='checkIcon'
                                        checked={checkList ? (checkList[record.sampleIds] === '00' || checkList[record.sampleIds] === '01' ? false : true) : false}
                                        onChange={checked => tagChange(record, checked, '02')}
                                    >
                                    </CheckableTag>
                                )
                            }}
                        />
                        <Table.Column
                            title='操作'
                            dataIndex='delete'
                            key='delete'
                            width='100px'
                            fixed='right'
                            render={(obj, record, index) => {
                                return (
                                    <Button type='primary' danger onClick={() => btnClick('delete', record)}>删除</Button>
                                )
                            }}
                        />
                    </Table>
                </div>
            </Modal>
        </div >
    )
}
