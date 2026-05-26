import React, { Component, Fragment } from 'react'
import ReactEcharts from 'echarts-for-react';
import echarts from 'echarts'
import { SelectWrap, InputWrap, MyButton } from '@/components/commonWrap'
import * as api from './services'
import '../index.less'
import { Button, Drawer, Empty, Input, Space, List, message, Pagination, Select } from 'antd';
import disasterType from '../disaster.json'
import { PlusCircleOutlined, MinusCircleOutlined } from '@ant-design/icons'

let myChart

export default class index extends Component {
    constructor(props) {
        super(props)

        this.state = {
            atlasJson: null,
            drawerVisible: false,
            selList: [...Array(5).keys()].map(i => {
                return ({
                    code: `${i + 1}`,
                    value: `第${i + 1}级`
                })
            }),
            entity: null,
            count: null,
            taskId: null,
            level: null,
            list: [],
            liIndex: null,
            liTaskId: null,
            liTypeCode: null,
            isShow: false,
            current: 1,
            pageSize: 5,
            total: 0,
            listData: [],
            start: '',
            end: '',
            middle: '',
            listLoading: false,
            selectIndex: null,
            liItem: null,
            categories: null,
            disasterType: disasterType,
            disaster: ''
        }
    }

    componentDidMount = () => {
        // const obj = {
        //     say: () => {
        //         setTimeout(() => {
        //             console.log(this)
        //         });
        //     }
        // }
        // obj.say();
        this.setState({
            disaster: this.state.disasterType[0].value,
            type: this.props.type
        }, () => {
            this.getList()
            myChart = echarts.init(document.getElementById('echarts-wrap'))
        })
    }

    getList = () => {
        api.getList().then(res => {
            const { data } = res
            this.setState({
                list: data,
                liIndex: data.length > 0 ? 0 : null,
                liTypeCode: data.length > 0 ? data[0].TYPE_CODE : null,
                liTaskId: data.length > 0 ? data[0].TASK_ID : null,
            }, () => {
                if (data.length === 0) {
                    myChart.hideLoading()
                }
                this.initData()
            })
        }).catch(err => { })
    }

    initListData = () => {
        const { start, end, current, liTaskId, disaster, middle, type } = this.state
        if (start && end) {
            this.setState({
                listLoading: true,
                listData: []
            })
            api.initListData(type === '01' ? {
                taskId: liTaskId,
                start,
                end,
                mid: middle,
                currentPage: current
            } : {
                    taskId: liTaskId,
                    start,
                    end,
                    mid: middle,
                    currentPage: current,
                    disaster
                }).then(res => {
                    const { totalRecord, data, categories } = res
                    this.setState({
                        total: totalRecord,
                        listData: data,
                        listLoading: false,
                        categories
                    })
                }).catch(err => { })
        } else {
            message.error('请输入起点和终点！')
        }
    }

    initData = () => {
        myChart.showLoading()
        const { liTaskId, count, entity, level, liTypeCode, disaster, type } = this.state
        api.initAtlas(type === '01' ?
            {
                taskId: liTaskId,
                count,
                entity,
                level,
                type: liTypeCode
            } : {
                taskId: liTaskId,
                count,
                entity,
                level,
                type: liTypeCode,
                disaster
            }).then(res => {
                this.setState({
                    atlasJson: res.data
                }, () => {
                    this.initChart()
                })
            }).catch(err => { })
    }


    initChart = () => {
        const { atlasJson, selectIndex, liItem, categories } = this.state
        if (atlasJson) {
            atlasJson.links.map(item => {
                item.lineStyle = {
                    color: "#3b3b3b"
                }
            })
        }
        let option = {
            legend: {
                type: 'scroll',
                orient: 'vertical',
                left: 40,
                top: 40,
                bottom: 20,
                data: selectIndex ? categories : (atlasJson ? atlasJson.categories : [])
            },

            tooltip: {
                formatter: function (param) {
                    if (param.dataType === 'node') {
                        return param.data.nodeName
                    } else {
                        return param.data.rel
                    }

                }
            },
            series: [{
                edgeSymbol: ['circle', 'arrow'],
                edgeSymbolSize: 6,
                type: 'graph',
                layout: 'force',
                animation: false,
                dataZoom: [
                    {
                        start: 65,
                        end: 85
                    }
                ],
                label: {
                    show: true,
                    position: 'right',
                    formatter: function (param) {
                        return param.data.nodeName
                    }
                },
                draggable: true,
                roam: true,
                symbolSize: 20,
                focusNodeAdjacency: true,
                data: selectIndex ? liItem.nodes : (atlasJson ? atlasJson.nodes : []),
                categories: selectIndex ? categories : (atlasJson ? atlasJson.categories : []),
                force: {
                    edgeLength: 5,
                    repulsion: 300,
                    gravity: 0.8
                },
                edges: selectIndex ? liItem.links : (atlasJson ? atlasJson.links : [])
            }]
        };
        myChart.setOption(option)
        myChart.hideLoading()
        window.addEventListener('resize', function () {
            myChart.resize()
        });
    }

    liClick(item, index) {
        const { TASK_ID, TYPE_CODE, selectIndex } = item
        this.setState({
            liIndex: index,
            liTypeCode: TYPE_CODE,
            liTaskId: TASK_ID,
            selectIndex: null,
            start: null,
            middle: null,
            end: null,
            listData: []
        }, () => {
            this.initData()
        })
    }

    inputChange = ({ target: { value } }) => {
        this.setState({
            entity: value
        })
    }

    searchInputChange(type, { target: { value } }) {
        if (type === 'start') {
            this.setState({
                start: value
            })
        } else if (type === 'end') {
            this.setState({
                end: value
            })
        } else {
            this.setState({
                middle: value
            })
        }
    }

    selChange(type, value, option) {
        if (type === 'count') {
            this.setState({ count: value }, () => {
                // this.initData()
            })
        } else if (type === 'disaster') {
            this.setState({ disaster: option.children }, () => {
                // this.initData()
            })
        } else {
            this.setState({ level: value }, () => {
                // this.initData()
            })
        }
    }

    onClose = () => {
        this.setState({
            drawerVisible: false
        })
    }

    showDrawer() {
        this.setState({
            drawerVisible: true
        })
    }
    iconAddClick(e) {
        e.stopPropagation()
        this.setState({
            isShow: !this.state.isShow
        })
    }

    pageChange = (page, pageSize) => {
        this.setState({
            current: page
        }, () => {
            this.initListData()
        })
    }
    listItemClick(item, index) {
        const { selectIndex } = this.state
        if (selectIndex !== index) {
            this.setState({
                selectIndex: index,
                liItem: item
            }, () => {
                this.initChart()
            })
        }
    }
    render() {
        const { listData, drawerVisible, type, list, liIndex, atlasJson, isShow, current, total, pageSize, listLoading, selectIndex, start, middle, end, disasterType } = this.state
        return (
            <div className='mds-showAtlas-wrap'>
                <div id='echarts-wrap'>
                    {
                        atlasJson && atlasJson.categories.length === 0 && atlasJson.links.length === 0 && atlasJson.nodes.length === 0
                            ?
                            <Empty style={{
                                position: 'absolute',
                                top: '50%',
                                left: '50%',
                                transform: 'translate(-50%, -50%)'
                            }} />
                            :
                            null
                    }
                </div>
                {
                    type === '01'
                        ?
                        <MyButton style={{ position: 'absolute', top: '0', left: '44px' }} label='图谱列表' onClick={this.showDrawer.bind(this)} />
                        :
                        null
                }
                <div className='mds-showAtlas-serchWrap'>
                    <div className='mds-showAtlas-serchBar'>
                        <Space direction='vertical'>
                            {
                                type === '01'
                                    ?
                                    null
                                    :
                                    <div className='mds-actionWrap' id='sel-wrap'>
                                        <label>
                                            灾情类别：
                                        </label>
                                        <Select
                                            style={{ width: '260px' }}
                                            onChange={(value, option) => this.selChange('disaster', value, option)}
                                            getPopupContainer={() => document.getElementById('sel-wrap')}
                                            defaultValue={disasterType[0].code}
                                        >
                                            {
                                                disasterType.map(item => {
                                                    return (
                                                        <Select.Option key={item.code} value={item.code} record={item}>
                                                            {item.value}
                                                        </Select.Option>
                                                    )
                                                })
                                            }
                                        </Select>
                                    </div >
                            }
                            <Input
                                placeholder="请输入起点"
                                allowClear
                                value={start}
                                onChange={this.searchInputChange.bind(this, 'start')}
                                suffix={!isShow ? <PlusCircleOutlined style={{ color: 'red' }} onClick={this.iconAddClick.bind(this)} /> : null}
                            />
                            {
                                isShow
                                    ?
                                    <Input
                                        placeholder="请输入途经点"
                                        allowClear
                                        value={middle}
                                        onChange={this.searchInputChange.bind(this, 'middle')}
                                        suffix={<MinusCircleOutlined style={{ color: 'red' }} onClick={this.iconAddClick.bind(this)} />}
                                    />
                                    :
                                    null
                            }
                            <Input
                                placeholder="请输入终点"
                                allowClear
                                value={end}
                                onChange={this.searchInputChange.bind(this, 'end')}
                            />
                            <Button type='primary' style={{ width: '100%' }} onClick={() => { this.initListData() }}>搜索</Button>
                        </Space>
                    </div>
                    <div className='mds-showAtlas-listWrap'>
                        <List
                            itemLayout="horizontal"
                            dataSource={listData}
                            loading={listLoading}
                            renderItem={(item, index) => (
                                <List.Item
                                    key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
                                >
                                    <List.Item.Meta
                                        className={
                                            selectIndex === (current - 1) * pageSize + index + 1
                                                ?
                                                'mds-showAtlas-liSelect'
                                                :
                                                null
                                        }
                                        onClick={this.listItemClick.bind(this, item, (current - 1) * pageSize + index + 1)}
                                        title={'路径' + ((current - 1) * pageSize + index + 1)}
                                        description={
                                            <div className='mds-showAtlas-listDescription'>
                                                <p>路径长度：{item.distance}</p>
                                                <p>途径结点：{item.path}</p>
                                            </div>
                                        }
                                    />
                                </List.Item>
                            )}
                        />
                    </div>
                    <Pagination
                        className='mds-showAtlas-pagination'
                        pageSize={pageSize}
                        current={current}
                        total={total}
                        onChange={this.pageChange}
                    />
                </div>
                <Drawer
                    title="图谱列表"
                    placement="left"
                    closable={false}
                    onClose={this.onClose}
                    visible={drawerVisible}
                    getContainer={false}
                    style={{ position: 'absolute' }}
                    drawerStyle={{
                        background: drawerVisible ? 'white' : '#F9FBFD'
                    }}
                    headerStyle={{
                        background: drawerVisible ? 'white' : '#F9FBFD',
                        borderBottom: drawerVisible ? '1px solid #f0f0f0' : 'none'
                    }}
                    bodyStyle={{
                        zIndex: drawerVisible ? '1' : '-1',
                    }}
                >
                    <ul className='mds-showAtlas-drawerUl'>
                        {
                            list.map((item, index) => {
                                return (
                                    <li
                                        key={Number(Math.random().toString().substr(3, 7) + Date.now()).toString(36)}
                                        className={
                                            liIndex === index
                                                ?
                                                'mds-showAtlas-liWrap mds-showAtlas-liSelect'
                                                :
                                                'mds-showAtlas-liWrap mds-showAtlas-liCommon'
                                        }
                                        onClick={this.liClick.bind(this, item, index)}
                                    >
                                        {item.TASK_NAME}
                                    </li>
                                )
                            })
                        }
                    </ul>
                </Drawer>
            </div>
        )
    }
}
