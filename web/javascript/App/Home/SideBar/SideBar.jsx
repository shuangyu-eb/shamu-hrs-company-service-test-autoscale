import React from 'react';
import { Link } from 'react-router-dom';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { MODULE } from '../Contants';
import './SideBar.scss';


const mapStateToProps = (store) => {
  const module = store.router.location.pathname.split('/')[1] || MODULE.DASHBOARD;
  return { module };
};

class SideBar extends React.Component {
  getClassName = (key) => {
    const { module } = this.props;
    return module === key ? 'item-selected' : '';
  }

  render() {
    return (
      <div className="sidebar">
        <div className="sidebar-inner">
          <div>
            <div className={`menu-item ${this.getClassName(MODULE.DASHBOARD)}`}>
              <Link to="/">
                <div className="menu-item-icon">
                  <svg width="24px" height="22px" viewBox="0 0 24 22">
                    <g id="1---Settings-+-Symbols" stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
                      <g id="⚙️/Navigation/Items/Dashboard/Default" transform="translate(-24.000000, -18.000000)" fill="#7B7E84" fillRule="nonzero">
                        <g id="item" transform="translate(11.000000, 18.000000)">
                          <path
                            d="M24.3284131,12.1363077 L24.3284131,13.5919629 L25.1150055,13.4876934 L26.2210046,11.6800834 C26.2763412,11.589643 26.3589951,11.5191345 26.4570265,11.4787432 L27.6684131,10.979623 L27.6684131,9.38123159 L25.9448191,10.499576 L25.6395377,11.2002127 C25.5997786,11.2914617 25.5336899,11.3687677 25.4497335,11.4222325 L24.3284131,12.1363077 Z M24.3284131,10.9507551 L24.7837804,10.6607697 L25.0881338,9.96226292 C25.1273138,9.87234288 25.192076,9.79593253 25.2743582,9.74254417 L27.6684131,8.1891749 L27.6684131,7.95064453 C27.6684131,7.58337517 27.9661437,7.28564453 28.3334131,7.28564453 C28.7006824,7.28564453 28.9984131,7.58337517 28.9984131,7.95064453 L28.9984131,18.6206445 C28.9984131,18.6217721 28.9984103,18.622899 28.9984047,18.6240253 C28.9984103,18.6251479 28.9984131,18.6262748 28.9984131,18.6274023 C28.9984131,18.9946717 28.7006824,19.2924023 28.3334131,19.2924023 L23.6634131,19.2924023 C23.2961437,19.2924023 22.9984131,18.9946717 22.9984131,18.6274023 C22.9984243,18.6251522 22.9984131,18.6228997 22.9984131,18.6206445 L22.9984131,7.95064453 C22.9984131,7.58337517 23.2961437,7.28564453 23.6634131,7.28564453 C24.0306824,7.28564453 24.3284131,7.58337517 24.3284131,7.95064453 L24.3284131,10.9507551 Z M24.3284131,14.6007105 L24.3284131,16.7438965 L24.570719,16.7438965 L26.4430172,14.5312694 L26.567465,14.4254962 L27.6684131,13.7649588 L27.6684131,12.0611791 L26.9888442,12.3411777 L25.8435669,14.212983 C25.7648543,14.3416284 25.6322797,14.427872 25.4827721,14.4476905 L24.3284131,14.6007105 Z M24.3284131,17.7438965 L24.3284131,17.9624023 L27.6684131,17.9624023 L27.6684131,14.9311345 L27.152922,15.2404144 L25.1842992,17.5668752 C25.0892971,17.6791459 24.9496843,17.7438965 24.8026125,17.7438965 L24.3284131,17.7438965 Z M19.9835986,15.3962012 L19.9835986,13.8078223 L16.6435986,13.8078223 L16.6435986,15.3962012 L19.9835986,15.3962012 Z M19.9835986,16.3962012 L16.6435986,16.3962012 L16.6435986,17.9556445 L19.9835986,17.9556445 L19.9835986,16.3962012 Z M19.9835986,12.8078223 L19.9835986,11.2194434 L16.6435986,11.2194434 L16.6435986,12.8078223 L19.9835986,12.8078223 Z M19.9835986,10.2194434 L19.9835986,8.61564453 L16.6435986,8.61564453 L16.6435986,10.2194434 L19.9835986,10.2194434 Z M14,0 L36,0 C36.5522847,-1.01453063e-16 37,0.44771525 37,1 L37,21 C37,21.5522847 36.5522847,22 36,22 L14,22 C13.4477153,22 13,21.5522847 13,21 L13,1 C13,0.44771525 13.4477153,1.01453063e-16 14,0 Z M14.33,1.33 L14.33,20.67 L35.67,20.67 L35.67,1.33 L14.33,1.33 Z M16.3135986,7.28564453 L20.3135986,7.28564453 C20.8658834,7.28564453 21.3135986,7.73335978 21.3135986,8.28564453 L21.3135986,18.2856445 C21.3135986,18.8379293 20.8658834,19.2856445 20.3135986,19.2856445 L16.3135986,19.2856445 C15.7613139,19.2856445 15.3135986,18.8379293 15.3135986,18.2856445 L15.3135986,8.28564453 C15.3135986,7.73335978 15.7613139,7.28564453 16.3135986,7.28564453 Z M32.6832275,13.4136296 C31.578658,13.4136296 30.6832275,12.5181991 30.6832275,11.4136296 C30.6832275,10.3090601 31.578658,9.41362956 32.6832275,9.41362956 C33.787797,9.41362956 34.6832275,10.3090601 34.6832275,11.4136296 C34.6832275,12.5181991 33.787797,13.4136296 32.6832275,13.4136296 Z M32.6832275,12.4136296 C33.2355123,12.4136296 33.6832275,11.9659143 33.6832275,11.4136296 C33.6832275,10.8613448 33.2355123,10.4136296 32.6832275,10.4136296 C32.1309428,10.4136296 31.6832275,10.8613448 31.6832275,11.4136296 C31.6832275,11.9659143 32.1309428,12.4136296 32.6832275,12.4136296 Z M31.1832275,7.28564453 C31.4593699,7.28564453 31.6832275,7.50950216 31.6832275,7.78564453 L31.6832275,8.28564453 C31.6832275,8.56178691 31.4593699,8.78564453 31.1832275,8.78564453 C30.9070852,8.78564453 30.6832275,8.56178691 30.6832275,8.28564453 L30.6832275,7.78564453 C30.6832275,7.50950216 30.9070852,7.28564453 31.1832275,7.28564453 Z M32.6832275,7.28564453 C32.9593699,7.28564453 33.1832275,7.50950216 33.1832275,7.78564453 L33.1832275,8.28564453 C33.1832275,8.56178691 32.9593699,8.78564453 32.6832275,8.78564453 C32.4070852,8.78564453 32.1832275,8.56178691 32.1832275,8.28564453 L32.1832275,7.78564453 C32.1832275,7.50950216 32.4070852,7.28564453 32.6832275,7.28564453 Z M34.1832275,7.28564453 C34.4593699,7.28564453 34.6832275,7.50950216 34.6832275,7.78564453 L34.6832275,8.28564453 C34.6832275,8.56178691 34.4593699,8.78564453 34.1832275,8.78564453 C33.9070852,8.78564453 33.6832275,8.56178691 33.6832275,8.28564453 L33.6832275,7.78564453 C33.6832275,7.50950216 33.9070852,7.28564453 34.1832275,7.28564453 Z M33.1832275,14.5 L34.1832275,14.5 C34.4593699,14.5 34.6832275,14.7238576 34.6832275,15 C34.6832275,15.2761424 34.4593699,15.5 34.1832275,15.5 L33.1832275,15.5 C32.9070852,15.5 32.6832275,15.2761424 32.6832275,15 C32.6832275,14.7238576 32.9070852,14.5 33.1832275,14.5 Z M31.1832275,16.3962012 L34.1832275,16.3962012 C34.4593699,16.3962012 34.6832275,16.6200588 34.6832275,16.8962012 C34.6832275,17.1723435 34.4593699,17.3962012 34.1832275,17.3962012 L31.1832275,17.3962012 C30.9070852,17.3962012 30.6832275,17.1723435 30.6832275,16.8962012 C30.6832275,16.6200588 30.9070852,16.3962012 31.1832275,16.3962012 Z M31.1832275,18.2924023 L34.1832275,18.2924023 C34.4593699,18.2924023 34.6832275,18.51626 34.6832275,18.7924023 C34.6832275,19.0685447 34.4593699,19.2924023 34.1832275,19.2924023 L31.1832275,19.2924023 C30.9070852,19.2924023 30.6832275,19.0685447 30.6832275,18.7924023 C30.6832275,18.51626 30.9070852,18.2924023 31.1832275,18.2924023 Z M16.3135986,2.39306641 L20.3135986,2.39306641 C20.8658834,2.39306641 21.3135986,2.84078166 21.3135986,3.39306641 L21.3135986,5.39306641 C21.3135986,5.94535116 20.8658834,6.39306641 20.3135986,6.39306641 L16.3135986,6.39306641 C15.7613139,6.39306641 15.3135986,5.94535116 15.3135986,5.39306641 L15.3135986,3.39306641 C15.3135986,2.84078166 15.7613139,2.39306641 16.3135986,2.39306641 Z M16.6485986,3.72806641 L16.6485986,5.05806641 L19.9785986,5.05806641 L19.9785986,3.72806641 L16.6485986,3.72806641 Z M23.9984131,2.39306641 L33.6832275,2.39306641 C34.2355123,2.39306641 34.6832275,2.84078166 34.6832275,3.39306641 L34.6832275,5.39306641 C34.6832275,5.94535116 34.2355123,6.39306641 33.6832275,6.39306641 L23.9984131,6.39306641 C23.4461283,6.39306641 22.9984131,5.94535116 22.9984131,5.39306641 L22.9984131,3.39306641 C22.9984131,2.84078166 23.4461283,2.39306641 23.9984131,2.39306641 Z M24.3408203,3.72806641 L24.3408203,5.05806641 L33.3408203,5.05806641 L33.3408203,3.72806641 L24.3408203,3.72806641 Z"
                            id="Combined-Shape-Copy"
                          />
                        </g>
                      </g>
                    </g>
                  </svg>
                </div>
                <span>Dashboard</span>
              </Link>
            </div>
            <div className={`menu-item ${this.getClassName(MODULE.MY_INFO)}`}>
              <Link to={`/${MODULE.MY_INFO}`}>
                <div className="menu-item-icon">
                  <svg width="20" height="17" viewBox="0 0 20 17">
                    <g fill="none" fillRule="evenodd" transform="translate(-2 -3)">
                      <rect width="24" height="24" />
                      <path
                        fill="#7B7E84"
                        fillRule="nonzero"
                        d="M3.13972603,19.9561644 L20.8767123,19.9561644 C21.2547945,19.9561644 21.5506849,19.660274 21.5506849,19.2821918 L21.5506849,7.31506849 C21.5506849,6.9369863 21.2547945,6.64109589 20.8767123,6.64109589 L15.7315068,6.64109589 L15.7315068,4.61917808 C15.7315068,4.24109589 15.4356164,3.94520548 15.0575342,3.94520548 L8.97534247,3.94520548 C8.59726027,3.94520548 8.30136986,4.24109589 8.30136986,4.61917808 L8.30136986,6.65753425 L3.13972603,6.65753425 C2.76164384,6.65753425 2.46575342,6.95342466 2.46575342,7.33150685 L2.46575342,19.2986301 C2.48219178,19.660274 2.77808219,19.9561644 3.13972603,19.9561644 Z M9.64931507,5.29315068 L14.3835616,5.29315068 L14.3835616,6.65753425 L9.64931507,6.65753425 L9.64931507,5.29315068 Z M3.81369863,7.9890411 L8.8109589,7.9890411 C8.86027397,8.00547945 8.90958904,8.00547945 8.95890411,8.00547945 C9.00821918,8.00547945 9.05753425,8.00547945 9.10684932,7.9890411 L14.8931507,7.9890411 C14.9424658,8.00547945 14.9917808,8.00547945 15.0410959,8.00547945 C15.090411,8.00547945 15.139726,8.00547945 15.1890411,7.9890411 L20.1863014,7.9890411 L20.1863014,12.6246575 L15.7315068,12.6246575 L15.7315068,11.8191781 C15.7315068,11.4410959 15.4356164,11.1452055 15.0575342,11.1452055 C14.6794521,11.1452055 14.3835616,11.4410959 14.3835616,11.8191781 L14.3835616,12.6246575 L9.64931507,12.6246575 L9.64931507,11.8191781 C9.64931507,11.4410959 9.35342466,11.1452055 8.97534247,11.1452055 C8.59726027,11.1452055 8.30136986,11.4410959 8.30136986,11.8191781 L8.30136986,12.6246575 L3.81369863,12.6246575 L3.81369863,7.9890411 Z M3.81369863,13.9726027 L8.30136986,13.9726027 L8.30136986,14.7287671 C8.30136986,15.1068493 8.59726027,15.4027397 8.97534247,15.4027397 C9.35342466,15.4027397 9.64931507,15.1068493 9.64931507,14.7287671 L9.64931507,13.9726027 L14.3835616,13.9726027 L14.3835616,14.7287671 C14.3835616,15.1068493 14.6794521,15.4027397 15.0575342,15.4027397 C15.4356164,15.4027397 15.7315068,15.1068493 15.7315068,14.7287671 L15.7315068,13.9726027 L20.2191781,13.9726027 L20.2191781,18.6082192 L3.81369863,18.6082192 L3.81369863,13.9726027 Z"
                      />
                    </g>
                  </svg>
                </div>
                <span>My Info</span>
              </Link>
            </div>
            <div className={`menu-item ${this.getClassName(MODULE.TIME_OFF)}`}>
              <Link to={`/${MODULE.TIME_OFF}`}>
                <div className="menu-item-icon">
                  <svg width="20" height="18" viewBox="0 0 20 18">
                    <g fill="none" fillRule="evenodd" transform="translate(-2 -3)">
                      <rect width="24" height="24" />
                      <g fill="#7B7E84" fillRule="nonzero" transform="translate(2 3)">
                        <rect width="5.333" height="1.333" x="12" y="13" />
                        <rect width="5.333" height="1.333" x="12" y="10" />
                        <rect width="5.333" height="1.333" x="12" y="7" />
                        <path
                          d="M19.3333333,2.66666667 L11.3333333,2.66666667 L11.3333333,4 L18.6666667,4 L18.6666667,16 L1.33333333,16 L1.33333333,4 L8.66666667,4 L8.66666667,2.66666667 L0.666666667,2.66666667 C0.298444444,2.66666667 0,2.96533333 0,3.33333333 L0,16.6666667 C0,17.0346667 0.298444444,17.3333333 0.666666667,17.3333333 L19.3333333,17.3333333 C19.7015556,17.3333333 20,17.0346667 20,16.6666667 L20,3.33333333 C20,2.96533333 19.7015556,2.66666667 19.3333333,2.66666667 Z"
                        />
                        <path
                          d="M8 4.44444444L8 0 12 0 12 4.44444444C12 4.81266667 11.7015556 5.11111111 11.3333333 5.11111111L8.66666667 5.11111111C8.29844444 5.11111111 8 4.81266667 8 4.44444444zM9.33333333 3.77777778L10.6666667 3.77777778 10.6666667 1.33333333 9.33333333 1.33333333 9.33333333 3.77777778zM10.6666667 11.5555556C10.6666667 10.4708889 10.0146667 9.53711111 9.08288889 9.12088889 9.242 8.77955556 9.33333333 8.40066667 9.33333333 8 9.33333333 6.52955556 8.13711111 5.33333333 6.66666667 5.33333333 5.19622222 5.33333333 4 6.52955556 4 8 4 8.40066667 4.09133333 8.77955556 4.25044444 9.12088889 3.31866667 9.53711111 2.66666667 10.4708889 2.66666667 11.5555556L2.66666667 14.6666667 10.6666667 14.6666667 10.6666667 11.5555556zM6.66666667 6.66666667C7.402 6.66666667 8 7.26466667 8 8 8 8.73533333 7.402 9.33333333 6.66666667 9.33333333 5.93133333 9.33333333 5.33333333 8.73533333 5.33333333 8 5.33333333 7.26466667 5.93133333 6.66666667 6.66666667 6.66666667zM9.33333333 13.3333333L4 13.3333333 4 11.5555556C4 10.8633333 4.53044444 10.2924444 5.20644444 10.2286667 5.62622222 10.5046667 6.12755556 10.6666667 6.66666667 10.6666667 7.20577778 10.6666667 7.70711111 10.5046667 8.12666667 10.2286667 8.80288889 10.2924444 9.33333333 10.8633333 9.33333333 11.5555556L9.33333333 13.3333333z"
                        />
                      </g>
                    </g>
                  </svg>
                </div>
                <span>Time Off</span>
              </Link>
            </div>
            <div className={`menu-item ${this.getClassName(MODULE.DOCUMENTS)}`}>
              <Link to={`/${MODULE.DOCUMENTS}`}>
                <div className="menu-item-icon">
                  <svg width="20" height="18" viewBox="0 0 20 18">
                    <g fill="none" fillRule="evenodd" transform="translate(-2 -4)">
                      <rect width="24" height="24" />
                      <g fill="#7B7E84" fillRule="nonzero" transform="translate(2 4)">
                        <g transform="translate(3 14)">
                          <path
                            d="M1.72656227.00671229694C1.86780295-.353162832 2.27403757-.530401071 2.6339127-.38916039 2.99378783-.24791971 3.17102607.158314918 3.02978539.518190047L2.15161156 2.75573887C2.01037088 3.115614 1.60413625 3.29285224 1.24426113 3.15161156.884385997 3.01037088.707147757 2.60413625.848388438 2.24426113L1.72656227.00671229694zM12.0586495.572917366C11.9271624.209365204 12.1152883-.191943428 12.4788404-.323430552 12.8423926-.454917676 13.2437012-.266791793 13.3751884.0967603688L14.1582694 2.2619215C14.2897565 2.62547366 14.1016307 3.0267823 13.7380785 3.15826942 13.3745263 3.28975654 12.9732177 3.10163066 12.8417306 2.7380785L12.0586495.572917366z"
                          />
                        </g>
                        <g transform="translate(4 5)">
                          <polygon points="3.977 1.051 1.706 4.677 .524 3.926 2.814 .27" />
                          <polygon points="5.489 .064 7.821 2.821 6.792 3.772 4.536 1.105" />
                          <polygon points="8.522 2.655 10.31 .068 11.468 .854 9.618 3.533" />
                        </g>
                        <path
                          d="M15.5555556,3.7037037 C15.147037,3.7037037 14.8148148,4.03592593 14.8148148,4.44444444 C14.8148148,4.85296296 15.147037,5.18518519 15.5555556,5.18518519 C15.9640741,5.18518519 16.2962963,4.85296296 16.2962963,4.44444444 C16.2962963,4.03592593 15.9640741,3.7037037 15.5555556,3.7037037 Z M15.5555556,2.3037037 C16.7372727,2.3037037 17.6962963,3.26272728 17.6962963,4.44444444 C17.6962963,5.62616161 16.7372727,6.58518519 15.5555556,6.58518519 C14.3738384,6.58518519 13.4148148,5.62616161 13.4148148,4.44444444 C13.4148148,3.26272728 14.3738384,2.3037037 15.5555556,2.3037037 Z M12.962963,9.25925926 C12.962963,8.85074074 12.6307407,8.51851852 12.2222222,8.51851852 C11.8137037,8.51851852 11.4814815,8.85074074 11.4814815,9.25925926 C11.4814815,9.66777778 11.8137037,10 12.2222222,10 C12.6307407,10 12.962963,9.66777778 12.962963,9.25925926 Z M14.362963,9.25925926 C14.362963,10.4409764 13.4039394,11.4 12.2222222,11.4 C11.0405051,11.4 10.0814815,10.4409764 10.0814815,9.25925926 C10.0814815,8.07754209 11.0405051,7.11851852 12.2222222,7.11851852 C13.4039394,7.11851852 14.362963,8.07754209 14.362963,9.25925926 Z M8.14814815,3.7037037 C7.73962963,3.7037037 7.40740741,4.03592593 7.40740741,4.44444444 C7.40740741,4.85296296 7.73962963,5.18518519 8.14814815,5.18518519 C8.55666667,5.18518519 8.88888889,4.85296296 8.88888889,4.44444444 C8.88888889,4.03592593 8.55666667,3.7037037 8.14814815,3.7037037 Z M8.14814815,2.3037037 C9.32986532,2.3037037 10.2888889,3.26272728 10.2888889,4.44444444 C10.2888889,5.62616161 9.32986532,6.58518519 8.14814815,6.58518519 C6.96643098,6.58518519 6.00740741,5.62616161 6.00740741,4.44444444 C6.00740741,3.26272728 6.96643098,2.3037037 8.14814815,2.3037037 Z M4.44444444,9.62962963 C4.03592593,9.62962963 3.7037037,9.96185185 3.7037037,10.3703704 C3.7037037,10.7788889 4.03592593,11.1111111 4.44444444,11.1111111 C4.85296296,11.1111111 5.18518519,10.7788889 5.18518519,10.3703704 C5.18518519,9.96185185 4.85296296,9.62962963 4.44444444,9.62962963 Z M4.44444444,8.22962963 C5.62616161,8.22962963 6.58518519,9.1886532 6.58518519,10.3703704 C6.58518519,11.5520875 5.62616161,12.5111111 4.44444444,12.5111111 C3.26272728,12.5111111 2.3037037,11.5520875 2.3037037,10.3703704 C2.3037037,9.1886532 3.26272728,8.22962963 4.44444444,8.22962963 Z"
                        />
                        <path
                          d="M1.4,1.4 L1.4,13.4000002 L18.6,13.4000002 L18.6,1.4 L1.4,1.4 Z M0.800000012,0 L19.2,0 C19.6418278,-8.11624513e-17 20,0.358172205 20,0.800000012 L20,14.0000002 C20,14.441828 19.6418278,14.8000002 19.2,14.8000002 L0.800000012,14.8000002 C0.358172205,14.8000002 1.03516311e-15,14.441828 0,14.0000002 L0,0.800000012 C-5.41083009e-17,0.358172205 0.358172205,8.11624513e-17 0.800000012,0 Z"
                        />
                      </g>
                    </g>
                  </svg>
                </div>
                <span>Documents</span>
              </Link>
            </div>
            <div className={`menu-item ${this.getClassName(MODULE.BENEFITS)}`}>
              <Link to={`/${MODULE.BENEFITS}`}>
                <div className="menu-item-icon">
                  <svg width="22" height="22" viewBox="0 0 22 22">
                    <g fill="none" fillRule="evenodd" transform="translate(-1 -1)">
                      <rect width="24" height="24" />
                      <g fill="#7B7E84" fillRule="nonzero" transform="translate(1 1)">
                        <path
                          d="M17.5080226,9.52903423 L17.2496212,8.75382998 C17.2243825,8.67811391 17.1924584,8.59712146 17.1361855,8.46447812 C17.0976664,8.37416151 17.0976664,8.37416151 17.0543291,8.27162631 C17.0187822,8.18670864 16.9892507,8.1131665 17.0200242,8.17332126 L16.9613614,8.03879504 L16.7073645,7.36146997 L17.112869,6.76242925 L18.4385274,4.80453748 L17.2262067,3.59221679 L14.58984,5.37683425 L13.8572343,5.0105314 C13.7940842,4.97895637 13.7124521,4.94423224 13.6013964,4.90176979 C13.5327519,4.87552335 13.2706073,4.77966451 13.2278716,4.76363863 L12.4949485,4.48879245 L12.3452759,3.72047296 L11.8991824,1.42426874 C11.8956842,1.40957628 11.3064126,1.4014867 10.1313675,1.4 L9.52903423,4.49197742 L8.75382998,4.75037884 C8.67811391,4.77561753 8.59712146,4.80754156 8.46447812,4.86381449 C8.37416151,4.90233357 8.37416151,4.90233357 8.27162631,4.94567086 C8.18670864,4.98121779 8.1131665,5.01074933 8.03879504,5.03863863 L7.36146997,5.29263553 L6.76242925,4.88713104 L4.82275692,3.57380873 L3.62277235,4.7737933 L5.40738981,7.41016 L5.04108696,8.1427657 C5.00951193,8.20591576 4.9747878,8.28754792 4.93232535,8.39860356 C4.9060789,8.46724811 4.81022007,8.72939268 4.79419418,8.77212837 L4.51934801,9.50505151 L3.75102851,9.65472414 L1.4408251,10.1040749 C1.41879903,10.1090808 1.40519066,10.6972667 1.4,11.8686325 L4.49197742,12.4709658 L4.75037884,13.24617 C4.77561753,13.3218861 4.80754156,13.4028785 4.86381449,13.5355219 C4.90233357,13.6258385 4.90233357,13.6258385 4.94567086,13.7283737 C4.98121779,13.8132914 5.01074933,13.8868335 5.03863863,13.961205 L5.29263553,14.63853 L4.88713104,15.2375708 L3.56147262,17.1954625 L4.7737933,18.4077832 L7.41016,16.6231657 L8.1427657,16.9894686 C8.20591576,17.0210436 8.28754792,17.0557678 8.39860356,17.0982302 C8.46724811,17.1244767 8.72939268,17.2203355 8.77212837,17.2363614 L9.50505151,17.5112075 L9.65472414,18.279527 L10.1007999,20.5694444 L11.8686325,20.5694444 L12.4709658,17.477467 L13.24617,17.2190656 C13.5017401,17.1338756 13.6720422,17.0667869 13.8266787,16.9894686 L14.5592844,16.6231657 L15.2375708,17.0823134 L17.1954625,18.4079718 L18.3954489,17.2079855 L16.6231657,14.58984 L16.9894686,13.8572343 C17.0210436,13.7940842 17.0557678,13.7124521 17.0982302,13.6013964 C17.1244767,13.5327519 17.2203355,13.2706073 17.2363614,13.2278716 L17.5112075,12.4949485 L18.279527,12.3452759 L20.5694839,11.8991924 L20.5709396,10.1257064 L17.5080226,9.52903423 Z M18.4729117,4.75375463 C18.4644574,4.76573153 18.4580478,4.77477289 18.4536999,4.78212892 L18.4729117,4.75375463 Z M20.9305556,8.76944444 C21.5416667,8.89166667 22,9.44166667 21.9694444,10.0833333 L21.9694444,11.9472222 C21.9694444,12.5888889 21.5416667,13.1388889 20.9,13.2611111 L18.5472222,13.7194444 C18.4555556,13.9638889 18.3638889,14.2388889 18.2416667,14.4833333 L19.5861111,16.4694444 C19.9527778,16.9888889 19.8916667,17.6916667 19.4333333,18.15 L18.15,19.4333333 C17.6916667,19.8611111 16.9583333,19.9527778 16.4388889,19.5861111 L14.4527778,18.2416667 C14.2083333,18.3638889 13.9638889,18.4555556 13.6888889,18.5472222 L13.2305556,20.9 C13.1083333,21.5111111 12.5583333,21.9694444 11.9166667,21.9694444 L10.0527778,21.9694444 C9.41111111,21.9694444 8.86111111,21.5416667 8.73888889,20.9 L8.28055556,18.5472222 C8.03611111,18.4555556 7.76111111,18.3638889 7.51666667,18.2416667 L5.53055556,19.5861111 C4.98055556,19.9527778 4.27777778,19.8916667 3.81944444,19.4333333 L2.53611111,18.15 C2.10833333,17.6916667 2.01666667,16.9583333 2.38333333,16.4388889 L3.72777778,14.4527778 C3.63611111,14.2083333 3.51388889,13.9638889 3.42222222,13.6888889 L1.06944444,13.2305556 C0.458333333,13.1083333 0,12.5583333 0,11.9166667 L0,10.0833333 C0,9.44166667 0.458333333,8.89166667 1.13055556,8.73888889 L3.48333333,8.28055556 C3.575,8.03611111 3.66666667,7.76111111 3.78888889,7.51666667 L2.44444444,5.53055556 C2.07777778,4.98055556 2.13888889,4.27777778 2.59722222,3.81944444 L3.88055556,2.53611111 C4.30833333,2.10833333 5.04166667,2.01666667 5.56111111,2.38333333 L7.54722222,3.72777778 C7.79166667,3.63611111 8.03611111,3.51388889 8.31111111,3.42222222 L8.76944444,1.06944444 C8.89166667,0.458333333 9.44166667,0 10.0833333,0 L11.9166667,0 C12.5583333,0 13.1083333,0.458333333 13.2611111,1.1 L13.7194444,3.45277778 C13.9638889,3.54444444 14.2388889,3.63611111 14.4833333,3.75833333 L16.4694444,2.41388889 C17.0194444,2.04722222 17.7222222,2.10833333 18.1805556,2.56666667 L19.4638889,3.85 C19.8916667,4.30833333 19.9833333,5.04166667 19.6166667,5.56111111 L18.2722222,7.54722222 C18.3638889,7.79166667 18.4861111,8.03611111 18.5777778,8.31111111 L20.9305556,8.76944444 Z"
                        />
                        <path
                          d="M11.0022222,6.57222222 C13.4371708,6.57222222 15.4322223,8.56608504 15.4322223,11 C15.4322223,13.433915 13.4371708,15.4277778 11.0022222,15.4277778 C8.56727364,15.4277778 6.57222222,13.433915 6.57222222,11 C6.57222222,8.56608504 8.56727364,6.57222222 11.0022222,6.57222222 Z M11.0022222,7.97222222 C9.34028558,7.97222222 7.97222222,9.33947052 7.97222222,11 C7.97222222,12.6605295 9.34028558,14.0277778 11.0022222,14.0277778 C12.6641589,14.0277778 14.0322223,12.6605295 14.0322223,11 C14.0322223,9.33947052 12.6641589,7.97222222 11.0022222,7.97222222 Z"
                        />
                      </g>
                    </g>
                  </svg>
                </div>
                <span>Benefits</span>
              </Link>
            </div>
            <div className={`menu-item ${this.getClassName(MODULE.EMPLOYEES)}`}>
              <Link to={`/${MODULE.EMPLOYEES}`}>
                <div className="menu-item-icon">
                  {/* <Icon type="usergroup-add" theme="outlined" /> */}
                </div>
                <span>Employee</span>
              </Link>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

SideBar.defaultProps = {
  module: MODULE.DASHBOARD,
};

SideBar.propTypes = {
  module: PropTypes.string,
};

export default connect(mapStateToProps)(SideBar);
