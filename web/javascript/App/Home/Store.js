import { createStore, applyMiddleware, compose } from 'redux';
import thunk from 'redux-thunk'; // allows async actions
import { createBrowserHistory } from 'history';
import { routerMiddleware } from 'connected-react-router';
import createRootReducer from './Reducers';

export const history = createBrowserHistory();

export default function configureStore(preloadedState) {
  const middleware = applyMiddleware(routerMiddleware(history), thunk);
  const store = createStore(
    createRootReducer(history), // root reducer with router state
    preloadedState,
    compose(middleware),
  );

  return store;
}