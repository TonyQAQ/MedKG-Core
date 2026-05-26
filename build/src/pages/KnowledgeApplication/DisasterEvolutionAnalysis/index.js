import React, { useEffect } from 'react'
import Echarts from 'echarts'
import 'echarts/map/js/china';
import geoJson from 'echarts/map/json/china.json';
import './index.less'

let myEchart

export default function Index() {

    const dealWithData = () => {
        const geoCoordMap = {

            大庆: [116.40529,39.904987]
        }
        let data = []
        for (let key in geoCoordMap) {
            data.push({ name: key, value: geoCoordMap[key] });
        }
        return data;
    }


    useEffect(() => {
        myEchart = Echarts.init(document.getElementById('mds-echarts-mapWrap'))
        const mapFeatures = Echarts.getMap('china').geoJson.features;
        debugger
        const dataValue = dealWithData();
        let option = {
            tooltip: {
                show: false
            },
            backgroundColor: '#012248',
            geo: {
                map: "china",
                roam: true,// 一定要关闭拖拽
                zoom: 1,
                // center: ['130.83531246', '36.0267395887'], // 调整地图位置
                left:'50',
                label: {
                    normal: {
                        show: false, //关闭省份名展示
                        fontSize: "10",
                        color: "rgba(0,0,0,0.7)"
                    },
                    emphasis: {
                        show: false
                    }
                },
                itemStyle: {
                    normal: {
                        areaColor: '#24CFF4',
                        borderColor: '#53D9FF',
                        borderWidth: 1.3,
                        shadowBlur: 15,
                        shadowColor: 'rgb(58,115,192)',
                        shadowOffsetX: 7,
                        shadowOffsetY: 6,
                    },
                    emphasis: {
                        areaColor: '#8dd7fc',
                        borderWidth: 1.6,
                        shadowBlur: 25,
                    }

                }
            },
            series: [{
                name: "",
                type: "scatter",
                coordinateSystem: "geo",
                data: dataValue,
                //   symbolSize: function(val) {
                //     return val[2] / 10;
                //   },
                symbol: "circle",
                symbolSize: 8,
                hoverSymbolSize: 10,
                tooltip: {
                    formatter(value) {
                        return value.data.name + "<br/>" + "设备数：" + "22";
                    },
                    show: true
                },
                encode: {
                    value: 2
                },
                label: {
                    formatter: "{b}",
                    position: "right",
                    show: false
                },
                itemStyle: {
                    color: "#0efacc"
                },
                emphasis: {
                    label: {
                        show: false
                    }
                }
            }
            ]
        };
        myEchart.setOption(option);
        setTimeout(function () {
            window.onresize = function () {
                myEchart.resize();
            }
        }, 200)
    }, [])

    return (
        <div id='mds-echarts-mapWrap'>
            ddd
        </div>
    )
}
