
package motorph_oop.model;

// Abstract Employee class.
// Serves as the base class for different employee types.
 
public abstract class Employee {

    // Employee Information
    private String empNo;
    private String lastName;
    private String firstName;
    private String birthday;
    private String address;
    private String phone;
    private String sss;
    private String philhealth;
    private String tin;
    private String pagibig;
    private String status;
    private String position;
    private String supervisor;

    // Financial Information
    private double basicSalary;
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;
    private double grossRate;
    private double hourlyRate;

    // Role association
    private Role role;

    // ABSTRACT METHOD

    // Computes salary based on hours worked.
    // Must be implemented by subclasses.
     
    public abstract double computeSalary(double hoursWorked);

    // METHOD OVERLOADING

    // Version 1 (Overloaded)
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Version 2 (Overloaded)
    public String getFullName(boolean lastNameFirst) {

        if (lastNameFirst) {
            return lastName + ", " + firstName;
        }

        return getFullName();
    }

    // Version 3 (Overloaded)
    public String getFullName(String middleInitial) {
        return firstName + " " + middleInitial + ". " + lastName;
    }

    // Role Getter and Setter
 
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }


    // GETTERS AND SETTERS

    public String getEmpNo() {
        return empNo;
    }

    public void setEmpNo(String empNo) {
        this.empNo = empNo;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSss() {
        return sss;
    }

    public void setSss(String sss) {
        this.sss = sss;
    }

    public String getPhilhealth() {
        return philhealth;
    }

    public void setPhilhealth(String philhealth) {
        this.philhealth = philhealth;
    }

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    public String getPagibig() {
        return pagibig;
    }

    public void setPagibig(String pagibig) {
        this.pagibig = pagibig;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(double basicSalary) {
        this.basicSalary = basicSalary;
    }

    public double getRiceSubsidy() {
        return riceSubsidy;
    }

    public void setRiceSubsidy(double riceSubsidy) {
        this.riceSubsidy = riceSubsidy;
    }

    public double getPhoneAllowance() {
        return phoneAllowance;
    }

    public void setPhoneAllowance(double phoneAllowance) {
        this.phoneAllowance = phoneAllowance;
    }

    public double getClothingAllowance() {
        return clothingAllowance;
    }

    public void setClothingAllowance(double clothingAllowance) {
        this.clothingAllowance = clothingAllowance;
    }

    public double getGrossRate() {
        return grossRate;
    }

    public void setGrossRate(double grossRate) {
        this.grossRate = grossRate;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    // METHOD OVERRIDING

    @Override
    public String toString() {
        return "Employee No: " + empNo +
               " | Name: " + getFullName(true) +
               " | Position: " + position +
               " | Status: " + status;
    }

    // CSV METHOD
    
    //Converts employee data to CSV format array.
    public String[] toCSVArray() {

        return new String[] {
                empNo,
                lastName,
                firstName,
                birthday,
                address,
                phone,
                sss,
                philhealth,
                tin,
                pagibig,
                status,
                position,
                supervisor,
                String.valueOf(basicSalary),
                String.valueOf(riceSubsidy),
                String.valueOf(phoneAllowance),
                String.valueOf(clothingAllowance),
                String.valueOf(grossRate),
                String.valueOf(hourlyRate)
        };
    }
}