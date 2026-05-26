import { Row, Col, Space, Table, Tooltip, Spin } from 'antd'
import React, { useEffect, useState } from 'react'
import Icon, { ShareAltOutlined, NodeIndexOutlined, DatabaseOutlined, ScheduleOutlined } from '@ant-design/icons';
import { Spring } from 'react-spring/renderprops'
import echarts from 'echarts'
import ReactEcharts from "echarts-for-react";
import * as api from './services'
import './index.less'
import 'echarts-wordcloud'

let flag, flag2

const Entity = () => (
    <svg t="1600845446034" width="1em" height="1em" fill="currentColor" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="9949" ><path d="M901.585455 678.493091l2.373818 4.096a34.909091 34.909091 0 0 1-11.496728 44.404364l-4.096 2.420363-349.090909 174.545455a34.909091 34.909091 0 0 1-26.205091 2.048l-5.026909-2.048-349.090909-174.545455a34.909091 34.909091 0 0 1 26.810182-64.279273l4.421818 1.861819 333.451637 166.679272 333.498181-166.725818a34.909091 34.909091 0 0 1 44.404364 11.543273z m0-174.545455l2.373818 4.096a34.909091 34.909091 0 0 1-11.496728 44.404364l-4.096 2.420364-349.090909 174.545454a34.909091 34.909091 0 0 1-26.205091 2.048l-5.026909-2.048-349.090909-174.545454a34.909091 34.909091 0 0 1 26.810182-64.279273l4.421818 1.861818 333.451637 166.679273 333.498181-166.725818a34.909091 34.909091 0 0 1 44.404364 11.543272z m-393.541819-360.634181a34.909091 34.909091 0 0 1 31.185455 0l349.090909 174.545454a34.909091 34.909091 0 0 1 0 62.464l-349.090909 174.545455a34.909091 34.909091 0 0 1-31.185455 0l-349.090909-174.545455a34.909091 34.909091 0 0 1 0-62.464z m15.592728 70.23709L252.555636 349.090909l271.080728 135.493818L794.670545 349.090909 523.636364 213.550545z" p-id="9950"></path></svg>
)

const Relation = () => (
    <svg t="1600844977378" width="1em" height="1em" fill="currentColor" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="9105" ><path d="M66.56 574.976v-453.12c0-30.208 24.576-55.296 55.296-55.296h453.12c30.208 0 55.296 24.576 55.296 55.296v453.12c0 30.208-24.576 55.296-55.296 55.296h-116.736c-18.432 0-33.28 14.848-33.28 33.28s14.848 33.28 33.28 33.28h134.144c57.344 0 104.448-46.592 104.448-104.448V104.448c0-57.344-46.592-103.936-103.936-103.936h-487.936c-57.344 0-103.936 46.592-103.936 103.936v487.936c0 57.344 46.592 104.448 104.448 104.448h159.232c18.432 0 33.28-14.848 33.28-33.28s-14.848-33.28-33.28-33.28H122.88c-31.744-0.512-56.32-25.088-56.32-55.296z" p-id="9106"></path><path d="M919.552 327.68h-159.232c-18.432 0-33.28 14.848-33.28 33.28s14.848 33.28 33.28 33.28h141.824c30.208 0 55.296 24.576 55.296 55.296v453.12c0 30.208-24.576 55.296-55.296 55.296h-453.12c-30.208 0-55.296-24.576-55.296-55.296v-453.632c0-30.208 24.576-55.296 55.296-55.296h116.736c18.432 0 33.28-14.848 33.28-33.28s-14.848-33.28-33.28-33.28h-134.144c-57.344 0-104.448 46.592-104.448 104.448V919.552c0 57.344 46.592 103.936 103.936 103.936h487.936c57.344 0 103.936-46.592 103.936-103.936v-487.936c1.024-57.344-45.568-103.936-103.424-103.936z" p-id="9107"></path></svg>
)

export default function Index() {
    const [numObj, setNumObj] = useState(null)
    const [markTaskList, setMarkTaskList] = useState([])
    const [wordCloudList, setWordCloudList] = useState([])
    const [recetTaskList, setRecetTaskList] = useState([])
    const [xData, setXData] = useState([])
    const [yData, setYData] = useState([])
    const [markTaskSpinning, setMarkTaskSpinning] = useState(true)

    useEffect(() => {
        getMiddleList()
        getFooterList()
        api.getHeadInfo().then(res => {
            const { data } = res
            setNumObj(data)
        }).catch(err => { })
    }, [])

    useEffect(() => {
        if (wordCloudList.length > 0) {
            const myChart = echarts.init(document.getElementById('wordCloud'))
            const option = getWordCloud()
            flag = setTimeout(() => {
                myChart.setOption(option)
            }, 0);
            function resizeFn() {
                if (flag2) {
                    clearTimeout(flag2)
                }
                flag2 = setTimeout(() => {
                    console.log(1111)
                    myChart.resize()
                }, 500);
            }
            window.addEventListener('resize', resizeFn);
            return () => {
                clearTimeout(flag)
                clearTimeout(flag2)
                window.removeEventListener('resize', resizeFn)
            }
        }
    }, [wordCloudList])

    // 获取中间内容列表
    const getMiddleList = () => {
        api.getMiddleList().then(res => {
            const { markTaskList, words } = res.data
            setMarkTaskList(markTaskList)
            setWordCloudList(words)
            setMarkTaskSpinning(false)
        }).catch(err => { })
    }

    // 获取下方内容列表
    const getFooterList = () => {
        api.getFooterList().then(res => {
            const { updateTaskList, taskStatList } = res.data
            setRecetTaskList(updateTaskList)
            setXData(taskStatList.xData)
            setYData(taskStatList.yData)
        }).catch(err => { })
    }

    // 柱状图
    const getOption = () => {
        return {
            color: ['#3398DB'],
            tooltip: {
                trigger: 'axis',
                axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                    type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                },
            },
            title: {
                text: '任务状态',
                left: 'center',
                top: 10,
                textStyle: {
                    color: 'rgba(0, 0, 0, 0.65)'
                }
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis: [
                {
                    type: 'category',
                    data: xData.length > 0 ? xData : [],
                    axisTick: {
                        alignWithLabel: true
                    }
                }
            ],
            yAxis: [
                {
                    type: 'value',
                    axisLine: {
                        show: false
                    },
                }
            ],
            series: [
                {
                    type: 'bar',
                    barWidth: '70px',
                    data: yData.length > 0 ? yData : [],
                    itemStyle: {
                        normal: {
                            color: function (params) {
                                const colorList = ['#52c41a', '#5A8BFF', ' #fba344', ' #696969', '#e64a19', '#00bcd4', '#c0ca33'];
                                const name = params.name;
                                if (name === '等待执行') {
                                    return colorList[0]
                                } else if (name === '规范制定') {
                                    return colorList[1]
                                } else if (name === '知识标注') {
                                    return colorList[2]
                                } else if (name === '任务结束') {
                                    return colorList[3]
                                } else if (name === '任务取消') {
                                    return colorList[4]
                                } else if (name === '句子切分') {
                                    return colorList[5]
                                } else if (name === '中文分词') {
                                    return colorList[6]
                                }
                            }
                        }
                    }
                }
            ]
        };
    }

    // 词云
    const getWordCloud = () => {
        const rgb = 155
        const c = Math.floor(Math.random() * (255 - rgb) + rgb)
        let maskImage = new Image();
        // 此为词云图呈现形状的图片base64码，一定要有，可以自定义图片
        maskImage.src =
            "data:image/png;base64,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";
        return {
            title: {
                text: '词云',
                left: 'center',
                top: 10,
                textStyle: {
                    color: 'rgba(0, 0, 0, 0.65)'
                }
            },
            tooltip: {
                pointFormat: "{series.name}: <b>{point.percentage:.1f}%</b>"
            },
            series: [{
                type: 'wordCloud',
                left: 'center',
                top: '-90',
                width: '100%',
                height: '170%',
                maskImage: maskImage,
                sizeRange: [12, 20],
                rotationRange: [0, 0],
                rotationStep: 45,
                gridSize: 8,
                textStyle: {
                    normal: {
                        fontFamily: 'sans-serif',
                        fontWeight: 'bold',
                        // Color can be a callback function or a color string
                        color: function () {
                            // Random color
                            return 'rgb(' + [
                                Math.floor(Math.random() * (c / 2)),
                                Math.floor(Math.random() * (255 - rgb) + rgb),
                                Math.floor(Math.random() * (255 - rgb) + rgb)
                            ].join(',') + ')';
                        }
                    },
                    emphasis: {
                        shadowBlur: 10,
                        shadowColor: '#333'
                    }
                },

                // Data is an array. Each array item must have name and value property.
                data: wordCloudList
            }]
        }
    }

    const getStatus = (type) => {
        let clsName
        switch (type) {
            case '已结束':
                clsName = 'status-end'
                break;
            case '等待执行':
                clsName = 'status-green'
                break;
            case '知识标注':
                clsName = 'status-label'
                break;
            case '规范制定':
                clsName = 'status-rule'
                break;
        }
        return (
            <span className={clsName}>{type}</span>
        )
    }

    return (
        <div className='mds-homePage-wrap'>
            <div className='mds-homePage-headInfo'>
                <Row gutter={[16, 16]} >
                    <Col xs={24} sm={24} md={12} lg={12} xl={6} >
                        <div className='mds-homePage-cardWrap'>
                            <div className='mds-homePage-cardWrapBg'>
                                <p className='mds-homePage-cardTitle'>
                                    知识图谱
                                </p>
                                <div className='mds-homePage-cardContentWrap'>
                                    <div className='mds-homePage-cardLeftContent'>
                                        <p>
                                            <Spring
                                                from={{ number: 0 }}
                                                to={{ number: numObj && numObj.mapTotalCount }}>
                                                {props => props.number.toFixed(0)}
                                            </Spring>
                                        </p>
                                        <p>总数</p>
                                    </div>
                                    <div className='mds-homePage-cardRightContent'>
                                        <ShareAltOutlined style={{ color: 'shite', fontSize: '50px' }} />
                                    </div>
                                </div>
                            </div>
                            <div className='mds-homePage-footer'>
                                <p>
                                    本月总数
                                    <span>|</span>
                                    <Spring
                                        from={{ number: 0 }}
                                        to={{ number: numObj && numObj.mapMonthCount }}>
                                        {props => props.number.toFixed(0)}
                                    </Spring>
                                </p>
                            </div>
                        </div>
                    </Col>
                    <Col xs={24} sm={24} md={12} lg={12} xl={6} >
                        <div className='mds-homePage-cardWrap'>
                            <div className='mds-homePage-cardWrapBg'>
                                <p className='mds-homePage-cardTitle'>
                                    实体
                                </p>
                                <div className='mds-homePage-cardContentWrap'>
                                    <div className='mds-homePage-cardLeftContent'>
                                        <p>
                                            <Spring
                                                from={{ number: 0 }}
                                                to={{ number: numObj && numObj.entityTotalCount }}>
                                                {props => props.number.toFixed(0)}
                                            </Spring>
                                        </p>
                                        <p>总数</p>
                                    </div>
                                    <div className='mds-homePage-cardRightContent'>
                                        <DatabaseOutlined style={{ color: 'shite', fontSize: '50px' }} />
                                    </div>
                                </div>
                            </div>
                            <div className='mds-homePage-footer'>
                                <p>
                                    本月总数
                                    <span>|</span>
                                    <Spring
                                        from={{ number: 0 }}
                                        to={{ number: numObj && numObj.entityMonthCount }}>
                                        {props => props.number.toFixed(0)}
                                    </Spring>
                                </p>
                            </div>
                        </div>
                    </Col>
                    <Col xs={24} sm={24} md={12} lg={12} xl={6} >
                        <div className='mds-homePage-cardWrap'>
                            <div className='mds-homePage-cardWrapBg'>
                                <p className='mds-homePage-cardTitle'>
                                    关系
                                </p>
                                <div className='mds-homePage-cardContentWrap'>
                                    <div className='mds-homePage-cardLeftContent'>
                                        <p>
                                            <Spring
                                                from={{ number: 0 }}
                                                to={{ number: numObj && numObj.relTotalCount }}>
                                                {props => props.number.toFixed(0)}
                                            </Spring>
                                        </p>
                                        <p>总数</p>
                                    </div>
                                    <div className='mds-homePage-cardRightContent'>
                                        <NodeIndexOutlined style={{ color: 'shite', fontSize: '50px' }} />
                                    </div>
                                </div>
                            </div>
                            <div className='mds-homePage-footer'>
                                <p>
                                    本月总数
                                    <span>|</span>
                                    <Spring
                                        from={{ number: 0 }}
                                        to={{ number: numObj && numObj.relMonthCount }}>
                                        {props => props.number.toFixed(0)}
                                    </Spring>
                                </p>
                            </div>
                        </div>
                    </Col>
                    <Col xs={24} sm={24} md={12} lg={12} xl={6} >
                        <div className='mds-homePage-cardWrap'>
                            <div className='mds-homePage-cardWrapBg'>
                                <p className='mds-homePage-cardTitle'>
                                    任务
                                </p>
                                <div className='mds-homePage-cardContentWrap'>
                                    <div className='mds-homePage-cardLeftContent'>
                                        <p>
                                            <Spring
                                                from={{ number: 0 }}
                                                to={{ number: numObj && numObj.taskTotalCount }}>
                                                {props => props.number.toFixed(0)}
                                            </Spring>
                                        </p>
                                        <p>总数</p>
                                    </div>
                                    <div className='mds-homePage-cardRightContent'>
                                        <ScheduleOutlined style={{ color: 'shite', fontSize: '50px' }} />
                                    </div>
                                </div>
                            </div>
                            <div className='mds-homePage-footer'>
                                <p>
                                    本月总数
                                    <span>|</span>
                                    <Spring
                                        from={{ number: 0 }}
                                        to={{ number: numObj && numObj.taskMonthCount }}>
                                        {props => props.number.toFixed(0)}
                                    </Spring>
                                </p>
                            </div>
                        </div>
                    </Col>
                </Row>
            </div>
            <div className='mds-homePage-middleWrap'>
                <Row gutter={[16, 16]}>
                    <Col xs={24} sm={24} md={24} lg={14} xl={14} >
                        <div className='msd-homePage-taskInfo'>
                            <p>任务标注情况</p>
                            <Spin spinning={markTaskSpinning}>
                                <ul className='msd-homePage-taskInfoUl'>
                                    <li>
                                        <label></label>
                                        <label className='msd-homePage-taskInfoLiIcon'><Icon component={Entity} style={{ fontSize: '40px', color: '#8a8a8a', marginRight: '10px' }} /><span>实体标签</span></label>
                                        <label className='msd-homePage-taskInfoLiIcon'><Icon component={Entity} style={{ fontSize: '40px', color: 'rgb(90 139 255)', marginRight: '10px' }} /><span>标注实体</span></label>
                                        <label className='msd-homePage-taskInfoLiIcon'><Icon component={Relation} style={{ fontSize: '32px', color: '#8a8a8a', marginRight: '10px' }} /><span>关系标签</span></label>
                                        <label className='msd-homePage-taskInfoLiIcon'><Icon component={Relation} style={{ fontSize: '32px', color: 'rgb(90 139 255)', marginRight: '10px' }} /><span>标注关系</span></label>
                                    </li>
                                    {
                                        markTaskList.map((i, index) => (
                                            <li key={`level-${index + 1}`}>
                                                <label><Tooltip title={i.taskName}> {i.taskName}</Tooltip> </label>
                                                <label>{i.entityLabelCount}</label>
                                                <label>{i.entityCount}</label>
                                                <label>{i.relLabelCount}</label>
                                                <label>{i.relCount}</label>
                                            </li>
                                        ))
                                    }
                                </ul>
                            </Spin>
                        </div>
                    </Col>
                    <Col xs={24} sm={24} md={24} lg={10} xl={10} >
                        <div id='wordCloud'></div>
                    </Col>
                </Row>
            </div>
            <div className='mds-homePage-footerWrap'>
                <Row gutter={[16, 16]}>
                    <Col xs={24} sm={24} md={24} lg={12} xl={12} >
                        <ReactEcharts
                            className='mds-homePage-charBar'
                            option={getOption()}
                            lazyUpdate={true}
                        />
                    </Col>
                    <Col xs={24} sm={24} md={24} lg={12} xl={12} >
                        <div className='mds-homePage-liList'>
                            <p>任务最新信息</p>
                            <ul>
                                {recetTaskList.map((i, index) => (
                                    <li className='mds-homePage-liItemWrap' key={`level-${index + 1}`}>
                                        <label><span className={index + 1 < 4 ? 'mds-homePage-liItem mds-homePage-liItemPoint' : 'mds-homePage-liItem'}>{index + 1}</span></label>
                                        <label>{i.taskName}</label>
                                        <label>{getStatus(i.stateName)}</label>
                                        <label> {i.lastUpdateDate}</label>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    </Col>
                </Row>
            </div>
        </div>
    )
}
