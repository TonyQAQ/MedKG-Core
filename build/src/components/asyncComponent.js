import React, { Component } from "react";
import { Spin } from "antd";
import { LoadingOutlined } from '@ant-design/icons';
import './index.less';

const antIcon = <LoadingOutlined style={{ fontSize: 48 }} spin />;

export default function asyncComponent(importComponent) {
  class AsyncComponent extends Component {
    constructor(props) {
      super(props);

      this.state = {
        component: null
      };
    }

    async componentDidMount() {
      const { default: component } = await importComponent();

      this.setState({
        component: component
      });
    }

    render() {
      const C = this.state.component;
      return C ? <C {...this.props} /> : <Spin indicator={antIcon} className='spin-wrap' />;
    }
  }

  return AsyncComponent;
}