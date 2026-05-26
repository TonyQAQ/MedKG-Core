import React, { Component } from 'react'
import ReactEcharts from 'echarts-for-react';
import echarts from 'echarts'
import { SelectWrap, InputWrap, MyButton } from '@/components/commonWrap'
import * as api from './services'
import '../index.less'
import { Button, Drawer, Empty, Select } from 'antd';
import disasterType from '../disaster.json'

let myChart

export default class index extends Component {
    constructor(props) {
        super(props)

        this.state = {
            atlasJson: null,
            atlasLoading: false,
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
            disasterType: disasterType,
            disaster: '',
            type: ''
        }
    }

    componentDidMount = () => {
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

    initData = () => {
        myChart.showLoading()
        const { liTaskId, count, entity, level, liTypeCode, disaster, type } = this.state
        api.initAtlas(type === '01' ? {
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
                    atlasJson: res.data,
                    atlasLoading: false
                }, () => {
                    this.initChart()
                })
            }).catch(err => { })
    }


    initChart = () => {
        const { atlasJson } = this.state
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
                data: atlasJson ? atlasJson.categories : []
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
                data: atlasJson ? atlasJson.nodes : [],
                categories: atlasJson ? atlasJson.categories : [],
                force: {
                    edgeLength: 5,
                    repulsion: 300,
                    gravity: 0.8
                },
                edges: atlasJson ? atlasJson.links : []
            }]
        };
        myChart.setOption(option)
        myChart.hideLoading()
        window.addEventListener('resize', function () {
            myChart.resize()
        });
    }

    liClick(item, index) {
        const { TASK_ID, TYPE_CODE } = item
        this.setState({
            liIndex: index,
            liTypeCode: TYPE_CODE,
            liTaskId: TASK_ID
        }, () => {
            this.initData()
        })
    }

    inputChange = ({ target: { value } }) => {
        this.setState({
            entity: value
        })
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
    render() {
        const { selList, drawerVisible, list, liIndex, atlasJson, disasterType, type } = this.state
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
                <div className='mds-showAtlas-serchBar'>
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
                                    style={{ width: '250px' }}
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
                            </div>
                    }
                    <InputWrap label='实体名' inputChange={this.inputChange.bind(this)} onPressEnter={() => {
                        this.setState({
                            atlasJson: null,
                            atlasLoading: true
                        }, () => {
                            this.initData()
                        })
                    }} />
                    <SelectWrap style={{ width: '250px' }} allowClear label='层级' list={selList} selChange={this.selChange.bind(this, 'level')} />
                    <SelectWrap style={{ width: '250px' }} allowClear label='返回条数' list={[
                        { value: '50', code: '50' },
                        { value: '100', code: '100' },
                        { value: '300', code: '300' },
                        { value: '500', code: '500' },
                        { value: '1000', code: '1000' }
                    ]} selChange={this.selChange.bind(this, 'count')} />
                    <Button type='primary' style={{ width: '100%', marginTop: '15px' }} onClick={() => {
                        this.setState({
                            atlasJson: null,
                            atlasLoading: true
                        }, () => {
                            this.initData()
                        })
                    }}>搜索</Button>
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
                                    <li className={liIndex === index ? 'mds-showAtlas-liWrap mds-showAtlas-liSelect' : 'mds-showAtlas-liWrap mds-showAtlas-liCommon'} onClick={this.liClick.bind(this, item, index)}>{item.TASK_NAME}</li>
                                )
                            })
                        }
                    </ul>
                </Drawer>
            </div>
        )
    }
}
