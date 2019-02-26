import { createStore, applyMiddleware, compose } from 'redux';
import thunk from 'redux-thunk'; // allows async actions
import rootReducer from './Reducers';

export default () => {
  const middleware = applyMiddleware(thunk);
  return createStore(rootReducer, compose(middleware));
};
