import { Router, useHistory, Link } from "react-router-dom";
import Header from "../../Component/GlobalStyles/Layout/DefaultLayout/Header";
import Footer from "../../Component/GlobalStyles/Layout/Footer";
import axios from "axios";
import "./usercart.scss";
import { useEffect, useState } from "react";

const images = {
  moneyI: require("./img/moneyI.png"),
  locationI: require("./img/locationI.png"),
  timeI: require("./img/timeI.png"),
};
function UserCart() {
  const [error, setError] = useState(null);
  const [isLoaded, setIsLoaded] = useState(false);
  const [plan, setPlan] = useState([]);

  let userCurrent = JSON.parse(localStorage.getItem("userCurrent"));
  useEffect(() => {
    fetch("http://localhost:8081/api/v1/plan/my-cart", {
      headers: {
        Authorization: `${userCurrent.token}`,
      },
    })
      .then((res) => res.json())
      .then(
        (result) => {
          setIsLoaded(true);
          setPlan(result);
          console.log(plan);
        },
        (error) => {
          setIsLoaded(true);
          setError(error);
        }
      );
  }, []);

  return (
    <div className="user-cart">
      <Header />
      <div className="cart-app">
        <div className="cart-container">
          <h3 className="cart__username">{userCurrent.name}'s cart</h3>

          {/* {`/planing-detail/${planingDetailId}`} */}
          {plan?.map((item) => {
            return (
              <Link
                className="link-container"
                to={`/planing-detail/${item.id}`}
              >
                <div className="cart-item">
                  <div className="cart-item-info">
                    <div className="cart-item-info-col1">
                      <h4 className="cart-item-info__title">{item.title}</h4>
                      <p className="cart-item-info__price">
                        <img src={images.moneyI} />
                        {item.expense}
                      </p>
                    </div>
                    <div className="cart-item-info-col2">
                      <h5 className="cart-item-info__name">
                        <img src={images.locationI} />
                        {item.destination}
                      </h5>
                      <p className="cart-item-info__date">
                        <img src={images.timeI} />
                        {item.beginDate} - {item.endDate}
                      </p>
                    </div>
                  </div>
                  <div className="cart-item-time">
                    <p>{item.createdAt}</p>
                  </div>
                </div>
              </Link>
            );
          })}
        </div>
      </div>
      <Footer />
    </div>
  );
}

export default UserCart;
