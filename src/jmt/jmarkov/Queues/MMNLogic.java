/**
 * 
 */
package jmt.jmarkov.Queues;

import jmt.jmarkov.Queues.Exceptions.NonErgodicException;

/**
 * @author arifc
 *
 */
public class MMNLogic extends MM1Logic {

	private int numberOfServer;
	
	/**
	 * @param lambda
	 * @param s
	 * @param aNumberOfServer
	 */
	public MMNLogic(double lambda, double s,int aNumberOfServer) {
		super(lambda, s);
		numberOfServer = aNumberOfServer;
	}

	public double getStatusProbability(int status) throws NonErgodicException{
		//Correct
		if(status <= numberOfServer)
			return pzero() * Math.pow(utilization(),(double)status) / factorial(status);
		else
			return pzero() * Math.pow((double)numberOfServer,(double)numberOfServer) *  
					Math.pow(utilization()/numberOfServer,(double)status) / factorial(numberOfServer);
		
	}
	
	private double pzero() throws NonErgodicException //pzero = (u+v) to the -1
	{
		return 1 / ( u() + v());
		
	}
	
	private double u() throws NonErgodicException{ //it is not utilization it is: sum from n=0 to m-1 ( (mp) to n / fact(n))
		double result=0;
		double utilization = utilization();//m*p
		double utilizationToN = 1;//for to the 0
		double factorialN=1;//for 0
		
		//bas case 0:
		result += utilizationToN / factorialN;
		
		for(int n=1;n<numberOfServer;n++)
		{
			utilizationToN *= utilization;
			factorialN *= n; 
			result += utilizationToN / factorialN;
		}
		return result;
	}
	
	private double v() throws NonErgodicException{// ((mp)to the m) / factorial(m) /(1-p) 
		return (Math.pow(utilization(),(double)numberOfServer)) / factorial(numberOfServer) / (1-(utilization()/numberOfServer));
		
	}
	
	private double Pq() throws NonErgodicException{//The probability Pq that upon an arrival all servers are busy and the customer has to wait is
		return v()/(u()+v());
	}

	public double mediaJobs() throws NonErgodicException{
		return utilization() + ( Pq() * (utilization() / numberOfServer) / (1- (utilization() / numberOfServer)) );	
	}
	
	/**
	 * Calcola il traffico offerto in base ai parametri
	 * lambda[job/s] e s[ms]
	 * 
	 * @return
	 */
	public double utilization() throws NonErgodicException{
		if((lambda * s) > numberOfServer){
			throw new NonErgodicException();	
		}
		else return (lambda * s);
	}
	
	
	/* (non-Javadoc)
	 * @see Queues.QueueLogic#throughput()
	 */
	public double throughput() throws NonErgodicException {
		double u = utilization();
		return lambda * u / u;
	}
	
	/* (non-Javadoc)
	 * @see Queues.QueueLogic#responseTime()
	 */
	public double responseTime() throws NonErgodicException {
		return 	mediaJobs() / lambda;
	}
	
		
	public int getNumberServer(){
		return numberOfServer;
	}
	
	private double factorial(int n) {
		double result = 1;
		for (int i = 2; i <= n; i++)
			result *= i;

		return result;
	}

}
